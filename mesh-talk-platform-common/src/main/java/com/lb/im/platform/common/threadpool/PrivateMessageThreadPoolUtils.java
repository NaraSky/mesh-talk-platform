package com.lb.im.platform.common.threadpool;

import java.util.concurrent.*;

/**
 * 私聊消息专用线程池工具类
 * 提供统一的线程资源管理，用于异步处理私聊消息相关任务
 * 如消息发送、状态更新、未读消息处理等
 */
public class PrivateMessageThreadPoolUtils {

    /**
     * 核心线程池执行器
     * 配置参数：
     * - 核心线程数：8（保持运行的最小线程数）
     * - 最大线程数：16（高负载时的最大线程数）
     * - 空闲线程存活时间：120秒
     * - 工作队列容量：4096个任务
     * - 拒绝策略：CallerRunsPolicy（线程池满时，由调用者线程执行任务）
     */
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(8,
                                                                                          16,
                                                                                          120,
                                                                                          TimeUnit.SECONDS,
                                                                                          new ArrayBlockingQueue<>(4096),
                                                                                          new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 执行无返回值的异步任务
     * 将任务提交到线程池中执行，不关心任务执行结果
     *
     * @param task 待执行的任务（Runnable接口实现）
     */
    public static void execute(Runnable task) {
        THREAD_POOL_EXECUTOR.execute(task);
    }

    /**
     * 执行有返回值的异步任务
     * 将任务提交到线程池中执行，并返回Future对象用于获取任务执行结果
     *
     * @param task 待执行的任务（Callable接口实现）
     * @param <T>  任务返回值类型
     * @return Future对象，可用于获取任务执行结果或取消任务
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return THREAD_POOL_EXECUTOR.submit(task);
    }

    /**
     * 优雅关闭线程池
     * 停止接收新任务，等待已提交任务完成后关闭线程池
     * 通常在应用程序关闭时调用，以释放线程资源
     */
    public static void shutdown() {
        THREAD_POOL_EXECUTOR.shutdown();
    }
}