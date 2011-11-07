package show

import org.multiverse.api.references.Ref
import static org.multiverse.api.StmUtils.newRef
import static groovyx.gpars.stm.GParsStm.*

class Buffer {

    final int x
    final int y = 100
    final int offset = 50

    Buffer(initX) { x = initX }
    
    private final Ref _sprites = newRef([].asImmutable())
    
    private List<TraySprite> getSprites() {
      _sprites.get()
    }
    
    private void setSprites(List<TraySprite> value) {
      _sprites.set(value.asImmutable())
    }

    void addSprite(done, TraySprite newEntry, inTime = 700) {
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

    void moveBottomTo(done, newX, newY, inTime = 700) {
        atomic {
          TraySprite mover = sprites[0]
          sprites = sprites - mover
          mover.moveTo done, newX, newY, inTime          
        }
    }
}
