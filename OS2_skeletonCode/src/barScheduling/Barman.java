import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/*
 Barman Thread class.
 */

public class Barman extends Thread {

	private CountDownLatch startSignal;
	private BlockingQueue<DrinkOrder> orderQueue;

	Barman(CountDownLatch startSignal, int schedAlg, int patronNum) {
		if (schedAlg == 0)
			this.orderQueue = new LinkedBlockingQueue<>();
		// FIX below
		else 
		{
			this.orderQueue = new PriorityBlockingQueue<>(patronNum,new Comparator<DrinkOrder>() {
				@Override
				public int compare(DrinkOrder order1, DrinkOrder order2) {
					return Integer.compare(order1.getExecutionTime(), order2.getExecutionTime());
				}
			});
		}


		this.startSignal = startSignal;
	}

	public void placeDrinkOrder(DrinkOrder order) throws InterruptedException {
		orderQueue.put(order);
	}

	public void run() {
		try {
			DrinkOrder nextOrder;

			startSignal.countDown(); // barman ready
			startSignal.await(); // check latch - don't start until told to do so

			while (true) {
				nextOrder = orderQueue.take();
				// Record the completion time
				long completionTime = System.currentTimeMillis();

				// Calculate turnaround time
				long turnaroundTime = completionTime - nextOrder.getPreparationTime();
				System.out.println(turnaroundTime);
				System.out.println("---Barman preparing order for patron " + nextOrder.toString());
				sleep(nextOrder.getExecutionTime()); // processing order
				System.out.println("---Barman has made order for patron " + nextOrder.toString());
				nextOrder.orderDone();
			}

		} catch (InterruptedException e1) {
			System.out.println("---Barman is packing up ");
		}
	}
}
