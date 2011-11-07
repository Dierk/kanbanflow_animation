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
import eu.hansolo.lightbulb.LightBulb
import eu.hansolo.lightbulb.JavaShadow
import eu.hansolo.steelseries.tools.Util

class ProcessingUnit {

    private static final Util UTIL = Util.INSTANCE

    BufferedImage image
    BufferedImage shinyImage

    volatile int x = 0
    volatile int y = 0
    int width = 160
    int height = 80
    Paint paint = Color.orange
    BufferedImage offBulb
    BufferedImage onBulb
    BufferedImage backBulb

    public ProcessingUnit(int x, int y) {
        this.x = x
        this.y = y
        image      = createImage(width, height)
        shinyImage = createImage(width, height, true)
        def bulb = new LightBulb()
        backBulb = bulb.createBulbImage(70,70)
        offBulb  = bulb.createOffImage(70,70)
        onBulb   = bulb.createOnImage(70,70,Color.yellow)
    }

    public BufferedImage createImage(final int WIDTH, final int HEIGHT, boolean shine = false) {
        final BufferedImage IMAGE = UTIL.createImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT)
        final Graphics2D G2 = IMAGE.createGraphics()
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        def shape = new RoundRectangle2D.Double(0d, 0d, IMAGE.getWidth(), IMAGE.getHeight(), 20d, 20d)
        if (shine) IMAGE = JavaShadow.INSTANCE.createDropShadow(shape, paint, 0, 1.0, Color.YELLOW, 14, 0)
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

    int getXLocation(){ x + 26 }
    int getYLocation(){ y + 15 }
}
