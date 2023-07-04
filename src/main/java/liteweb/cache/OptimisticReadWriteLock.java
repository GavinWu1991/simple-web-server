package liteweb.cache;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class OptimisticReadWriteLock implements ReadWriteLock {

    private final ReadLock readerLock;
    private final WriteLock writerLock;

    public OptimisticReadWriteLock() {
        WriteSync writeSync = new WriteSync();
        ReadSync readSync = new ReadSync(writeSync);

        this.readerLock = new ReadLock(readSync);
        this.writerLock = new WriteLock(writeSync);
    }

    public WriteLock writeLock() {
        return writerLock;
    }

    public ReadLock readLock() {
        return readerLock;
    }

    private static class ReadSync extends AbstractQueuedSynchronizer {

        private final WriteSync writeSync;

        private ReadSync(WriteSync writeSync) {
            this.writeSync = writeSync;
        }

        @Override
        protected boolean tryAcquire(int arg) {
            return writeSync.getInnerState() == 0;
        }

        @Override
        protected boolean tryRelease(int arg) {
            // always true as the reader will not block itself
            return true;
        }

        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

    }

    private static class WriteSync extends AbstractQueuedSynchronizer {

        @Override
        protected boolean tryAcquire(int arg) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == 0) {
                throw new IllegalMonitorStateException();
            }
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        int getInnerState() {
            return getState();
        }
    }

    /**
     * The lock returned by method {@link OptimisticReadWriteLock#readLock}.
     */
    public static class ReadLock implements Lock {
        private final ReadSync sync;

        protected ReadLock(ReadSync sync) {
            this.sync = sync;
        }

        public void lock() {
            sync.tryAcquire(1);
        }

        public void lockInterruptibly() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }

        public boolean tryLock() {
            return sync.tryAcquire(1);
        }

        public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }

        public void unlock() {
            sync.release(1);
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * The lock returned by method {@link OptimisticReadWriteLock#writeLock}.
     */
    public static class WriteLock implements Lock {
        private final WriteSync sync;

        /**
         * Constructor for use by subclasses
         */
        protected WriteLock(WriteSync sync) {
            this.sync = sync;
        }

        public void lock() {
            sync.acquire(1);
        }

        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        public boolean tryLock() {
            return sync.tryAcquire(1);
        }

        public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }

        public void unlock() {
            sync.release(1);
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }
}
