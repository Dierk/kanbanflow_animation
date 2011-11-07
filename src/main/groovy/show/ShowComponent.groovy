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

    final Ref _traySprites = newRef([].asImmutable())
    final Buffer upstream
    final Buffer downstream
    final Ref _producers = newRef([].asImmutable())
    final Ref _consumers = newRef([].asImmutable())
    final Ref _units = newRef([].asImmutable())
    final def producerLabelImage = ProcessingUnit.createLabelImage(160,80,"Producer")
    final def consumerLabelImage = ProcessingUnit.createLabelImage(160,80,"Consumer")
    
    private void setTraySprites(value) {
      atomic {
        _traySprites.set(value.asImmutable())
      }
    }
    
    private List<TraySprite> getTraySprites() { atomic { _traySprites.get() } }

    private void setProducers(value) {
      atomic {
        _producers.set(value.asImmutable())
      }
    }
    
    private List<ProcessingUnit> getProducers() { atomic { _producers.get() } }

    private void setConsumers(value) {
      atomic {
        _consumers.set(value.asImmutable())
      }
    }

    private List<ProcessingUnit> getConsumers() { atomic { _consumers.get() } }

    private void setUnits(value) {
      atomic {
        _units.set(value.asImmutable())
      }
    }

    private List<ProcessingUnit> getUnits() { atomic { _units.get() } }

    ShowComponent(params) {
        super()
        params.wip.times { traySprites = traySprites  + new TraySprite(this) }
        producers  = (1 .. params.producers).collect { new ProcessingUnit(180, it * 100) }
        consumers  = (1 .. params.consumers).collect { new ProcessingUnit(580, it * 100) }
        units      = producers + consumers
        upstream   = new Buffer(0)
        downstream = new Buffer(400)        
    }

    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D G2 = g.create()
        
        drawTransformed(G2, producerLabelImage){ it.translate 180, 20 }
        drawTransformed(G2, consumerLabelImage){ it.translate 580, 20 }

        units.each { processingUnit ->
            BufferedImage toPaint = processingUnit.image
            def x = processingUnit.x
            def y = processingUnit.y
            if (thereIsATrayInside(processingUnit)) {
                toPaint = processingUnit.shinyImage
                x -= 14
                y -= 14
            }
            drawTransformed(G2, toPaint) { AffineTransform txf ->
                txf.translate x,y
            }
        }
        consumers.each { processingUnit ->
            Closure bulbTransform = { AffineTransform txf ->
                txf.translate processingUnit.x - 44, processingUnit.y + 75
                txf.rotate(Math.toRadians(270))
            }
            BufferedImage toPaint = processingUnit.offBulb
            if (thereIsATrayInside(processingUnit)) { toPaint = processingUnit.onBulb }
            drawTransformed G2, toPaint, bulbTransform
            drawTransformed G2, processingUnit.backBulb, bulbTransform
        }
        traySprites.each { sprite ->
            drawTransformed(G2, sprite.battery) { AffineTransform txf ->
                if (sprite.x > 400){
                    double frac = Math.min( 1d, (sprite.x - 400) / 206)
                    txf.translate sprite.x + frac*120, sprite.y + frac*50
                    txf.rotate(Math.toRadians( frac* 180  ))
                    return
                }
                txf.translate sprite.x, sprite.y
            }
        }
        G2.dispose()
    }

    private boolean thereIsATrayInside(processingUnit){
        traySprites.any {
            (0 .. 50).containsWithinBounds(it.x - processingUnit.x) &&
            (0 .. processingUnit.height).containsWithinBounds(it.y - processingUnit.y)
        }
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

