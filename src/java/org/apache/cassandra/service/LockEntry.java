package org.apache.cassandra.service;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class LockEntry {
	public int state;
    public Lock lock;
    public Condition cond;
    public Condition replyBlock;
    public int in;
    public int out;
    public int replyBufferSize;
    public long timestamp;
    
    public LockEntry(int s, Lock l, Condition c, Condition b, int i, int o){
    	state = s;
    	lock = l;
    	cond = c;
    	replyBlock = b;
    	in = i;
    	out = o;
    	replyBufferSize = 0;
    	timestamp = 0;
    }
}
