package show

import javax.swing.JComponent
import org.pushingpixels.trident.Timeline
import org.pushingpixels.trident.ease.Spline
import groovy.beans.Bindable
import eu.hansolo.steelseries.extras.Battery
import java.util.concurrent.CountDownLatch
import org.pushingpixels.trident.callback.TimelineCallbackAdapter
import org.pushingpixels.trident.Timeline.TimelineState
import org.pushingpixels.trident.callback.TimelineCallback

import org.multiverse.api.references.Ref
import org.multiverse.api.references.IntRef
import org.multiverse.api.references.BooleanRef
import static org.multiverse.api.StmUtils.newRef
import static org.multiverse.api.StmUtils.newIntRef
import static org.multiverse.api.StmUtils.newBooleanRef
import static groovyx.gpars.stm.GParsStm.*

import java.awt.image.BufferedImage

@Bindable
class TraySprite {

    //Map images = [:]
    //volatile x = 0
    //volatile int y = -100 // be invisible at startup
    //volatile int payload = 0
    //volatile boolean visible = true

    final Ref _battery = newRef()
    final IntRef _x = newIntRef(0)
    final IntRef _y = newIntRef(-100) // be invisible at startup
    final BooleanRef _visible = newBooleanRef(true)
    final IntRef _payload = newIntRef(0)

    void setBattery(BufferedImage value) {
      def currentValue = getBattery()
      atomic {  _battery.set(value) }
      firePropertyChange('battery', currentValue, getBattery() )
    }

    BufferedImage getBattery() { atomic { _battery.get() }  }

    void setX(int value) {
      def currentValue = getX()
      atomic {  _x.set(value) }
      firePropertyChange('x', currentValue, getX() )
    }

    int getX() { atomic { _x.get() } }
    
    void setY(int value) {
      def currentValue = getY()
      atomic {  _y.set(value) }
      firePropertyChange('y', currentValue, getY() )
    }

    int getY() { atomic { _y.get() } }
    
    void setVisible(boolean value) {
      def currentValue = getVisible()
      atomic {  _visible.set(value) }
      firePropertyChange('visible', currentValue, getVisible() )
    }

    boolean getVisible() { atomic { _visible.get() } }
    
    void setPayload(int value) {
      def currentValue = getPayload()
      atomic {  _payload.set(value) }
      firePropertyChange('payload', currentValue, getPayload() )
    }
    
    int getPayload() { atomic { _payload.get() } }

    public TraySprite(JComponent caller) {

        int width = 120
        int height = 50
        
        //def battery = new Battery()
        //images.battery = battery.create_BATTERY_Image(width, height, payload)
        
        def batteryFactory = new Battery()
        battery = batteryFactory.create_BATTERY_Image(width, height, payload)

        propertyChange =  { e ->
            if (e.propertyName == "payload"){
                //images.battery = battery.create_BATTERY_Image(width, height, payload)
                battery = batteryFactory.create_BATTERY_Image(width, height, payload)
            }
            caller.repaint()
        }

    }

    TraySprite moveTo(done, newX, newY, inTime=1000){
        def x = this.x
        def y = this.y
        new Timeline(this).with {
            addPropertyToInterpolate 'x', x, newX
            addPropertyToInterpolate 'y', y, newY
            ease = new Spline(0.5f)
            duration = inTime
            addCallback(doneCallback(done))
            play()
        }
        return this
    }

    TraySprite charge(done, from, to, inTime=2000) {
        new Timeline(this).with {
            addPropertyToInterpolate 'payload', from, to
            duration = inTime
            addCallback(doneCallback(done))
            play()
        }
        return this
    }

    protected TimelineCallback doneCallback(CountDownLatch done) {
        return [
                onTimelineStateChanged: {oldState, newState, durationFraction, timelinePosition ->
                    if (oldState == TimelineState.DONE && newState == TimelineState.IDLE) {
                        done?.countDown()
                    }
                }
        ] as TimelineCallbackAdapter
    }
}
