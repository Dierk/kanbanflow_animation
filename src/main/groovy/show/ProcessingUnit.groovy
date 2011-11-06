package show

import java.awt.image.BufferedImage
import java.awt.Transparency
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import java.awt.Color
import java.awt.Paint
import java.awt.font.FontRenderContext
import java.awt.Font
import java.awt.font.TextLayout

class ProcessingUnit {

    private static final eu.hansolo.steelseries.tools.Util UTIL = eu.hansolo.steelseries.tools.Util.INSTANCE;

    final BufferedImage image

    //volatile int x = 0
    //volatile int y = 0
    final int x = 0
    final int y = 0
    final int width = 160
    final int height = 80
    final Paint paint = Color.orange

    public ProcessingUnit(int x, int y) {
        this.x = x
        this.y = y
        image = createImage(width, height)
    }

    public BufferedImage createImage(final int WIDTH, final int HEIGHT) {
        final BufferedImage IMAGE = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT)
        final Graphics2D G2 = IMAGE.createGraphics()
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        def shape = new RoundRectangle2D.Double(0d, 0d, IMAGE.getWidth(), IMAGE.getHeight(), 20d, 20d)
        G2.paint = paint
        G2.fill shape
        G2.dispose()
        return IMAGE
    }

    public static BufferedImage createLabelImage(final int WIDTH, final int HEIGHT, String label) {
        final BufferedImage IMAGE = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT)
        final Graphics2D G2 = IMAGE.createGraphics()
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        final FontRenderContext RENDER_CONTEXT = new FontRenderContext()
        G2.setColor(Color.red.darker().darker())
        G2.setFont(new Font("Myriad Pro", 0, (int) (0.12 * WIDTH)))
        final TextLayout LAYOUT_HELLO__WORLD_ = new TextLayout(label, G2.getFont(), RENDER_CONTEXT)
        def x = (0.25f * WIDTH).toFloat()
        def y = (0.45f * HEIGHT + LAYOUT_HELLO__WORLD_.getAscent() - LAYOUT_HELLO__WORLD_.getDescent()).toFloat()
        G2.drawString(label, x, y)

        G2.dispose()
        return IMAGE
    }

    List getProductLocation() { [x + 26, y + 15] }
}
