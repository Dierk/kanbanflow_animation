package show

import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D

import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import javax.swing.JComponent

import org.multiverse.api.references.Ref
import static org.multiverse.api.StmUtils.newRef
import static groovyx.gpars.stm.GParsStm.*


public class ShowComponent extends JComponent {

    //final List<TraySprite> traySprites = [].asSynchronized()
    final Ref _traySprites = newRef(Collections.unmodifiableList([]))
    final Buffer upstream
    final Buffer downstream
    //final List<ProcessingUnit> producers
    //final List<ProcessingUnit> consumers
    //final List<ProcessingUnit> units
    final Ref _producers = newRef(Collections.unmodifiableList([]))
    final Ref _consumers = newRef(Collections.unmodifiableList([]))
    final Ref _units = newRef(Collections.unmodifiableList([]))
    final def producerLabelImage = ProcessingUnit.createLabelImage(160,80,"Producer")
    final def consumerLabelImage = ProcessingUnit.createLabelImage(160,80,"Consumer")
    
    private void setTraySprites(value) {
      atomic {
        _traySprites.set(Collections.unmodifiableList(value))
      }
    }
    
    private List<TraySprite> getTraySprites() { atomic { _traySprites.get() } }

    private void setProducers(value) {
      atomic {
        _producers.set(Collections.unmodifiableList(value))
      }
    }
    
    private List<ProcessingUnit> getProducers() { atomic { _producers.get() } }

    private void setConsumers(value) {
      atomic {
        _consumers.set(Collections.unmodifiableList(value))
      }
    }

    private List<ProcessingUnit> getConsumers() { atomic { _consumers.get() } }

    private void setUnits(value) {
      atomic {
        _units.set(Collections.unmodifiableList(value))
      }
    }

    private List<ProcessingUnit> getUnits() { atomic { _units.get() } }

    ShowComponent(params) {
        super()
        //params.wip.times { traySprites << new TraySprite(this) }
        params.wip.times { traySprites = traySprites  + new TraySprite(this) }
        producers = (1 .. params.producers).collect { new ProcessingUnit(180, it * 100) }
        consumers = (1 .. params.consumers).collect { new ProcessingUnit(580, it * 100) }
        units = producers + consumers
        //upstream   = new Buffer(x:  0, y:100, offset:50)
        //downstream = new Buffer(x:400, y:100, offset:50)
        
        upstream   = new Buffer(0)
        downstream = new Buffer(400)        
    }


    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D G2 = g.create()
        
        drawTransformed(G2, producerLabelImage){ it.translate 180, 20 }
        drawTransformed(G2, consumerLabelImage){ it.translate 580, 20 }

        units.each { processingUnit ->
            drawTransformed(G2, processingUnit.image) { AffineTransform txf ->
                txf.translate processingUnit.x, processingUnit.y
            }
        }
        traySprites.each { sprite ->
            //sprite.images.each { String name, BufferedImage image ->
                drawTransformed(G2, sprite.battery) { AffineTransform txf ->
                    if (sprite.x > 400){
                        double frac = Math.min( 1d, (sprite.x - 400) / 206)
                        txf.translate sprite.x + frac*120, sprite.y + frac*50
                        txf.rotate(Math.toRadians( frac* 180  ))
                        return
                    }
                    txf.translate sprite.x, sprite.y
                }
            //}
        }
        G2.dispose()
    }


    Dimension getMinimumSize() { [10,10] }

    private void drawTransformed(Graphics2D G2, BufferedImage image, Closure transform) {
        def oldTf = G2.transform
        AffineTransform txf = new AffineTransform(oldTf)

        transform txf

        G2.transform = txf
        G2.drawImage image, 0, 0, (ImageObserver) null
        G2.transform = oldTf
    }
}

