package com.datastax.demo.utils;

public class Timer {

	private long timeTaken;
	private long start;
	
	public Timer(){
		start();
	}
	public void start(){
		this.start = System.currentTimeMillis();
	}
	public void end(){
		this.timeTaken = System.currentTimeMillis() - start;
	}
	
	public long getTimeTakenMillis(){
		return this.timeTaken;
	}
	
	public int getTimeTakenSeconds(){
		return new Double(this.timeTaken / 1000).intValue();
	}
	
	public String getTimeTakenMinutes(){
		return String.format("%1$,.2f", new Double(this.timeTaken / (1000*60)));
	}

}
