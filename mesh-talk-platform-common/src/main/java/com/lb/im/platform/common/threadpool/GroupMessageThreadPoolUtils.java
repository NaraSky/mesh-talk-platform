package com.lb.im.platform.common.threadpool;

import java.util.concurrent.*;

/**
 * 线程池工具类
 * 提供全局共享的线程池实例，用于异步任务的提交和执行，避免重复创建线程资源
 * 技术点：
 * 1. 使用静态单例模式，确保应用中只有一个线程池实例
 * 2. 通过ThreadPoolExecutor自定义线程池参数，实现线程资源的高效利用
 * 3. 支持任务提交和线程池关闭的统一管理
 */
public class GroupMessageThreadPoolUtils {
    /**
     * 线程池执行器实例
     * 核心线程数: 8 - 保持活跃的基本线程数
     * 最大线程数: 16 - 高负载时的最大线程数
     * 空闲线程保持时间: 120秒 - 超过核心线程的空闲线程在此时间后被回收
     * 工作队列: 容量为4096的ArrayBlockingQueue - 当所有线程都在工作时，新任务将被放入此队列
     * 拒绝策略: CallerRunsPolicy - 当队列满时，任务会在调用者线程中执行，避免任务丢失
     */
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(8,
                                                                                          16,
                                                                                          120,
                                                                                          TimeUnit.SECONDS,
                                                                                          new ArrayBlockingQueue<>(4096),
                                                                                          new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 执行无返回值的异步任务
     *
     * @param task 需要执行的任务，实现了Runnable接口
     */
    public static void execute(Runnable task) {
        THREAD_POOL_EXECUTOR.execute(task);
    }

    /**
     * 提交有返回值的异步任务
     *
     * @param task 需要执行的任务，实现了Callable接口
     * @param <T>  任务执行完成后的返回值类型
     * @return Future对象，可用于获取任务执行结果或取消任务
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return THREAD_POOL_EXECUTOR.submit(task);
    }

    /**
     * 关闭线程池
     * 调用后线程池将不再接受新任务，但会等待已提交任务完成
     * 通常在应用关闭时调用此方法释放资源
     */
    public static void shutdown() {
        THREAD_POOL_EXECUTOR.shutdown();
    }
}