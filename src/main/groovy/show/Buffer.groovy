package show

//import groovy.transform.WithWriteLock
import org.multiverse.api.references.Ref
import static org.multiverse.api.StmUtils.newRef
import static groovyx.gpars.stm.GParsStm.*

class Buffer {

    final int x
    final int y = 100
    final int offset = 50

    Buffer(initX) { x = initX }
    
    //protected final List<TraySprite> sprites = []
    //private final List<TraySprite> sprites = []
    private final Ref _sprites = newRef(Collections.unmodifiableList([]))
    
    private List<TraySprite> getSprites() {
      _sprites.get()
    }
    
    private void setSprites(List<TraySprite> value) {
      _sprites.set(Collections.unmodifiableList(value))
    }

    //@WithWriteLock
    void addSprite(done, TraySprite newEntry, inTime = 700) {
        //sprites.retainAll { it.visible }
        //sprites.add newEntry
        
        atomic { 
          sprites = sprites + newEntry
        
          int add = sprites.size() * offset
          sprites.eachWithIndex { TraySprite sprite, int idx ->
              add -= offset
              def waiter = (sprite == newEntry) ? done : null
              sprite.moveTo waiter, x, y + add, inTime
          }
        }
    }

    //@WithWriteLock
    void moveBottomTo(done, newX, newY, inTime = 700) {
        //sprites.retainAll { it.visible }
        
        atomic {
          TraySprite mover = sprites[0]
          sprites = sprites - mover
          mover.moveTo done, newX, newY, inTime          
        }
        //return mover
    }
}
