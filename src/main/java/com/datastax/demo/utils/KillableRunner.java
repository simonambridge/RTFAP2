package com.datastax.demo.utils;

public interface KillableRunner extends Runnable {

	public void shutdown();
}
