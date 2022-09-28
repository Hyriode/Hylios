package fr.hyriode.hylios.queue;

import fr.hyriode.hylios.api.queue.QueueGroup;

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by AstFaster
 * on 17/04/2022 at 09:39
 */
class QueueHandler extends PriorityBlockingQueue<QueueGroup> {

    private final ReentrantLock lock = new ReentrantLock();

    public QueueHandler() {
        super(100000, Comparator.comparingInt(QueueGroup::getPriority));
    }

    public void drainGroups(Collection<QueueGroup> collection, int maxElements) {
        if (collection == null) {
            throw new NullPointerException();
        }
        if (maxElements <= 0) {
            return;
        }

        this.lock.lock();

        try {
            int remainingElements = maxElements;
            int groupIndex = 0;

            for (int i = 0; i < this.size(); i++) {
                final QueueGroup group = (QueueGroup) this.toArray()[groupIndex];
                final int groupSize = group.getSize();

                if (remainingElements - groupSize >= 0) {
                    collection.add(group);

                    remainingElements -= groupSize;
                } else {
                    groupIndex++;
                }

                if (remainingElements <= 0) {
                    break;
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

}
