/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cassandra.db;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.apache.cassandra.batchlog.LegacyBatchlogMigrator;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.exceptions.WriteTimeoutException;
import org.apache.cassandra.io.util.FastByteArrayInputStream;
import org.apache.cassandra.net.*;
import org.apache.cassandra.net.MessagingService.Verb;
import org.apache.cassandra.service.LockEntry;
import org.apache.cassandra.tracing.Tracing;

public class MutationVerbHandler implements IVerbHandler<Mutation>
{
    private void reply(int id, InetAddress replyTo)
    {
        Tracing.trace("Enqueuing response to {}", replyTo);
        MessagingService.instance().sendReply(WriteResponse.createMessage(), id, replyTo);
    }

    private void failed()
    {
        Tracing.trace("Payload application resulted in WriteTimeout, not replying");
    }
    /*add*/

    public void doVerbLock(MessageIn<Mutation> message, int id)  throws IOException
    {
    	int key = message.payload.key().getToken().hashCode();
    	long msgTimestamp = message.payload.createdAt;
    	InetAddress msgaddr = message.from;
    	int msgAddrHash = msgaddr.hashCode();
    	
    	//global data kept in DatabaseDescriptor
    	Lock glock = DatabaseDescriptor.glock;
    	HashMap<Integer, LockEntry> lockmap = DatabaseDescriptor.lockmap;
    	
    	//prepare hashmap
    	glock.lock();
    	System.out.println("Asdsadsafs");
    	if ( !lockmap.containsKey(key) ){
    		Lock tmpLock = new ReentrantLock();
    		Condition tmpCond = tmpLock.newCondition();
    		Condition blockCond = tmpLock.newCondition();
    		lockmap.put(key,  new LockEntry(-1, tmpLock, tmpCond, blockCond, 0, 0));
    	}
    	LockEntry entry = lockmap.get(key);
    	glock.unlock();
    	
    	
    	//block or reply?
    	boolean blocked = false; 
    	entry.lock.lock();
    	long myTimestamp = entry.timestamp;
		InetAddress myaddr = DatabaseDescriptor.getListenAddress();
		int myAddrHash = myaddr.hashCode();
    	if(entry.state == 1){
    		entry.replyBufferSize++;
    		blocked = true;
    		try {
				while(entry.state==1)
					entry.replyBlock.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		entry.replyBufferSize--;
    	}else if(entry.state == 0){
    		
    		
    		if(myTimestamp < msgTimestamp || (myTimestamp==msgTimestamp && myAddrHash < msgAddrHash)){
    			entry.replyBufferSize++;
    			blocked = true;
    			try{
    				while(entry.state != -1)
    					entry.replyBlock.await();
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
        		entry.replyBufferSize--;
    		}
    	}
    	
    	replyLock(message, id);
    	entry.lock.unlock();
    	
    	glock.lock();
    	if(entry.state == -1 && entry.in==0 && entry.out==0 && entry.replyBufferSize==0)
    		lockmap.remove(key);
    	glock.unlock();
    }
    
    
    
    public void replyLock(MessageIn<Mutation> message, int id)  throws IOException
    {
        // Check if there were any forwarding headers in this message
        byte[] from = message.parameters.get(Mutation.FORWARD_FROM);
        InetAddress replyTo;
        if (from == null)
        {
            replyTo = message.from;
            byte[] forwardBytes = message.parameters.get(Mutation.FORWARD_TO);
            if (forwardBytes != null)
                forwardToLocalNodes(message.payload, message.verb, forwardBytes, message.from);
        }
        else
        {
            replyTo = InetAddress.getByAddress(from);
        }

        try
        {
                reply(id, replyTo);
        }
        catch (WriteTimeoutException wto)
        {
            failed();
        }
    }
    /*add*/

    
    public void doVerb(MessageIn<Mutation> message, int id)  throws IOException
    {
        
    	
    	/*add*/
        if(message.verb==Verb.LOCK)
        {
            doVerbLock(message, id);
            return;
        }
        
        /*add*/
        
        // Check if there were any forwarding headers in this message
        byte[] from = message.parameters.get(Mutation.FORWARD_FROM);
        InetAddress replyTo;
        if (from == null)
        {
            replyTo = message.from;
            byte[] forwardBytes = message.parameters.get(Mutation.FORWARD_TO);
            if (forwardBytes != null)
                forwardToLocalNodes(message.payload, message.verb, forwardBytes, message.from);
        }
        else
        {
            replyTo = InetAddress.getByAddress(from);
        }

        try
        {
            if (message.version < MessagingService.VERSION_30 && LegacyBatchlogMigrator.isLegacyBatchlogMutation(message.payload))
            {
                LegacyBatchlogMigrator.handleLegacyMutation(message.payload);
                reply(id, replyTo);
            }
            else
                message.payload.applyFuture().thenAccept(o -> reply(id, replyTo)).exceptionally(wto -> {
                    failed();
                    return null;
                });
        }
        catch (WriteTimeoutException wto)
        {
            failed();
        }
    }

    /**
     * Older version (< 1.0) will not send this message at all, hence we don't
     * need to check the version of the data.
     */
    private static void forwardToLocalNodes(Mutation mutation, MessagingService.Verb verb, byte[] forwardBytes, InetAddress from) throws IOException
    {
        try (DataInputStream in = new DataInputStream(new FastByteArrayInputStream(forwardBytes)))
        {
            int size = in.readInt();

            // tell the recipients who to send their ack to
            MessageOut<Mutation> message = new MessageOut<>(verb, mutation, Mutation.serializer).withParameter(Mutation.FORWARD_FROM, from.getAddress());
            // Send a message to each of the addresses on our Forward List
            for (int i = 0; i < size; i++)
            {
                InetAddress address = CompactEndpointSerializationHelper.deserialize(in);
                int id = in.readInt();
                Tracing.trace("Enqueuing forwarded write to {}", address);
                MessagingService.instance().sendOneWay(message, id, address);
            }
        }
    }
}
