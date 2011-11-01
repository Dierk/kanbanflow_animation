package kanban

import javax.swing.SwingUtilities
import show.ShowComponent
import static groovyx.gpars.dataflow.Dataflow.operator
import java.util.concurrent.CountDownLatch
import show.Buffer
import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import show.TraySprite
import groovyx.gpars.dataflow.DataflowQueue
import groovyx.gpars.dataflow.operator.DataflowProcessor

class Tray {
    volatile int card
    volatile def product
}

class Kanban {


    DataflowQueue upstream   = new DataflowQueue()                   // empty trays travel back upstream to the producer
    DataflowQueue downstream = new DataflowQueue()                   // trays with products travel to the consumer downstream

    ShowComponent showComponent
    Buffer showUp
    Buffer showDn
    int wip
    int producers
    int consumers
    int moveTime
    int produceTime
    int consumeTime
    boolean log

    ReentrantReadWriteLock animationSync = new ReentrantReadWriteLock()

    volatile boolean dropNext = false

    void setShowComponent(ShowComponent show) {
        showComponent = show
        showUp = showComponent.upstream
        showDn = showComponent.downstream
    }

    void visualize(action) {
        def done = new CountDownLatch(1)
        SwingUtilities.invokeLater {
            action(done)
        }
        done.await()
    }

    @WithReadLock('animationSync')
    void fetchFromUpstream(producer) {
        visualize { showUp.moveBottomTo(it, *producer.productLocation, moveTime ) }
    }

    @WithReadLock('animationSync')
    void produce(mover) {
        visualize { mover.charge it, 0, 100, produceTime }
    }

    @WithWriteLock('animationSync')
    void sendDown(mover) {
        visualize { showDn.addSprite it, mover, moveTime }
    }

    @WithWriteLock('animationSync')
    void fetchFromDownstream(consumer) {
        visualize { showDn.moveBottomTo(it, *consumer.productLocation, moveTime) }
    }

    void consume(mover) {
        visualize { mover.charge it, 100, 0, consumeTime }
    }

    void sendUp(mover) {
        visualize { mover.moveTo it, mover.x + 200, mover.y,    moveTime * 0.2 }
        visualize { mover.moveTo it, mover.x,       0,          moveTime * 0.3 }
        visualize { mover.moveTo it, 0,             mover.y,    moveTime * 1.0 }
        visualize { showUp.addSprite it, mover,                 moveTime * 0.4 }
    }

    def run() {

        def prodWiring = [inputs: [upstream], outputs: [downstream]] // maxForks is optional
        def consWiring = [inputs: [downstream], outputs: [upstream]] // maxForks is optional

        println()
        wip.times {
            def product = showComponent.traySprites[it]
            visualize { showUp.addSprite it, product, moveTime * 0.4}
            if (log) print "tray$it "
            upstream << new Tray(card: it, product: product)
        }
        println()

        producers.times {
            makeProducer(prodWiring, downstream, showComponent.producers[it], it)
        }

        consumers.times {
            makeConsumer(consWiring, upstream, showComponent.consumers[it], it)
        }
    }

    protected def makeProducer(prodWiring, downstream, producer, number) {
        operator prodWiring, { tray ->
            if (dropNext) {
                dropNext = false
                drop tray.product
                return
            }
            printInfo 'p', number, '+', tray
            fetchFromUpstream producer
            produce     tray.product
            sendDown    tray.product
            printInfo   'p', number, '-', tray
            downstream << tray                                  // send tray with product inside to consumer
        }
    }

    protected DataflowProcessor makeConsumer(consWiring, upstream, consumer, number) {
        operator consWiring, { tray ->
            printInfo 'c', number, '+', tray
            fetchFromDownstream consumer
            consume     tray.product
            sendUp      tray.product
            printInfo   'c', number, '-', tray
            upstream << tray                                    // send empty tray back upstream
        }
    }
    private void printInfo(prefix,number,action,tray){
        if (log) println('      ' * tray.card + "$prefix$number$action$tray.card")
    }

    void plusWip() {
        SwingUtilities.invokeLater {
            def sprite = new TraySprite(showComponent)
            showComponent.traySprites << sprite
            def done = new CountDownLatch(1)
            showUp.addSprite done, sprite, moveTime
            done.await()
            upstream << new Tray(card: System.currentTimeMillis(), product: sprite)
        }
    }

    void minusWip() {
        dropNext = true
    }

    @WithReadLock('animationSync')
    void drop(TraySprite mover){
        mover.visible = false
        showComponent.traySprites.retainAll { it.visible }
    }
}

