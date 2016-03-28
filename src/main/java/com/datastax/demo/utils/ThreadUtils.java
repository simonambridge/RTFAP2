package com.datastax.demo.utils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {

	public static void shutdown(List<KillableRunner> tasks, ExecutorService executor) {
		
		for (KillableRunner task : tasks){
			task.shutdown();
		}
				
		executor.shutdown();
		try {
			executor.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
