package show


class Buffer {

    int x
    int y
    int offset

    protected final List<TraySprite> sprites = []

    void addSprite(done, TraySprite newEntry, inTime = 700) {
        sprites.retainAll { it.visible }
        sprites.add newEntry
        int add = sprites.size() * offset
        sprites.eachWithIndex { TraySprite sprite, int idx ->
            add -= offset
            def waiter = (sprite == newEntry) ? done : null
            sprite.moveTo waiter, x, y + add, inTime
        }
    }

    TraySprite moveBottomTo(done, newX, newY, inTime = 700) {
        sprites.retainAll { it.visible }
        TraySprite mover = sprites.remove(0)
        mover.moveTo done, newX, newY, inTime
        return mover
    }
}
