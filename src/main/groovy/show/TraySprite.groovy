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

@Bindable
class TraySprite {

    Map images = [:]

    int x = 0
    int y = -100 // be invisible at startup
    int payload = 0
    boolean visible = true

    public TraySprite(JComponent caller) {

        int width = 120
        int height = 50
        def battery = new Battery()
        images.battery = battery.create_BATTERY_Image(width, height, payload)

        propertyChange =  { e ->
            if (e.propertyName == "payload"){
                images.battery = battery.create_BATTERY_Image(width, height, payload)
            }
            caller.repaint()
        }

    }

    TraySprite moveTo(done, newX, newY, inTime=1000){
        def timeline = new Timeline(this)
        timeline.addPropertyToInterpolate 'x', x, newX
        timeline.addPropertyToInterpolate 'y', y, newY
        timeline.ease = new Spline(0.5f)
        timeline.duration = inTime
        timeline.addCallback(doneCallback(done))
        timeline.play()
        return this
    }

    TraySprite charge(done, from, to, inTime=2000) {
        def timeline = new Timeline(this)
        timeline.addPropertyToInterpolate 'payload', from, to
        timeline.duration = inTime
        timeline.addCallback(doneCallback(done))
        timeline.play()
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
