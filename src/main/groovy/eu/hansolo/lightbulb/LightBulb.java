package eu.hansolo.lightbulb;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.GeneralPath;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.AttributedString;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class LightBulb extends JComponent {
    public static final String STATE_PROPERTY = "state";
    private boolean on;
    private float alpha;
    private int direction;
    private PropertyChangeSupport propertySupport;
    private final Rectangle INNER_BOUNDS = new Rectangle(0, 0, 114, 114);
    private final Point2D CENTER;
    private Color glowColor;
    private BufferedImage offImage;
    private BufferedImage onImage;
    private BufferedImage bulbImage;

    private boolean square;
    private transient final ComponentListener COMPONENT_LISTENER = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent event) {
            final int SIZE = getWidth() <= getHeight() ? getWidth() : getHeight();
            Container parent = getParent();
            if ((parent != null) && (parent.getLayout() == null)) {
                if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                    setSize(getMinimumSize());
                } else if(square) {
					setSize(SIZE, SIZE);
				} else {
                    setSize(getWidth(), getHeight());
                }
            } else {
                if (SIZE < getMinimumSize().width || SIZE < getMinimumSize().height) {
                    setPreferredSize(getMinimumSize());
                } else if(square) {
					setPreferredSize(new Dimension(SIZE, SIZE));
				} else {
                    setPreferredSize(new Dimension(getWidth(), getHeight()));
                }
            }
            calcInnerBounds();
            init(getInnerBounds().width, getInnerBounds().height);
        }
    };

    public LightBulb() {
        super();
        addComponentListener(COMPONENT_LISTENER);
        propertySupport = new PropertyChangeSupport(this);
        CENTER = new Point2D.Double();
        offImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        onImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        bulbImage = createImage(INNER_BOUNDS.width, INNER_BOUNDS.height, Transparency.TRANSLUCENT);
        alpha = 1.0f;
        direction = SwingUtilities.NORTH;
        glowColor = new Color(1.0f, 1.0f, 0.0f);
        square = true;

    }

    public final void init(final int WIDTH, final int HEIGHT) {
        if (WIDTH <= 1 || HEIGHT <= 1) {
            return;
        }
        if (offImage != null) {
            offImage.flush();
        }
        offImage = createOffImage(WIDTH, HEIGHT);
        if (onImage != null) {
            onImage.flush();
        }
        onImage = createOnImage(WIDTH, HEIGHT, glowColor);
        if (bulbImage != null) {
            bulbImage.flush();
        }
        bulbImage = createBulbImage(WIDTH, HEIGHT);

        CENTER.setLocation(WIDTH / 2.0, HEIGHT / 2.0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Create the Graphics2D object
        final Graphics2D G2 = (Graphics2D) g.create();

        // Set the rendering hints
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Take direction into account
        switch (direction) {
            case SwingUtilities.SOUTH:
                G2.rotate(Math.PI, CENTER.getX(), CENTER.getY());
                break;
            case SwingUtilities.EAST:
                G2.rotate(-Math.PI / 2, CENTER.getX(), CENTER.getY());
                break;
            case SwingUtilities.WEST:
                G2.rotate(Math.PI / 2, CENTER.getX(), CENTER.getY());
                break;
        }

        // Take insets into account (e.g. used by borders)
        G2.translate(getInnerBounds().x, getInnerBounds().y);

        if (on) {
            G2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f - alpha));
            G2.drawImage(offImage, 0, 0, null);
            G2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            G2.drawImage(onImage, 0, 0, null);
            G2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            G2.drawImage(offImage, 0, 0, null);
        }
        G2.drawImage(bulbImage, 0, 0, null);

        // Dispose the temp graphics object
        G2.dispose();
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(final boolean ON) {
        boolean oldState = on;
        on = ON;
        propertySupport.firePropertyChange(STATE_PROPERTY, oldState, on);
        repaint(getInnerBounds());
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(final float ALPHA) {
        alpha = ALPHA < 0 ? 0 : (ALPHA > 1 ? 1: ALPHA);
        repaint(getInnerBounds());
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(final int DIRECTION) {
        switch (DIRECTION) {
            case SwingUtilities.SOUTH:
                direction = SwingUtilities.SOUTH;
                break;
            case SwingUtilities.EAST:
                direction = SwingUtilities.EAST;
                break;
            case SwingUtilities.WEST:
                direction = SwingUtilities.WEST;
                break;
            case SwingUtilities.NORTH:
            default:
                direction = SwingUtilities.NORTH;
                break;
        }
        repaint(getInnerBounds());
    }

    public Color getGlowColor() {
        return glowColor;
    }

    public void setGlowColor(final Color GLOW_COLOR) {
        glowColor = GLOW_COLOR;
        init(getInnerBounds().width, getInnerBounds().height);
        repaint(getInnerBounds());
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener LISTENER) {
        if (isShowing()) {
            propertySupport.addPropertyChangeListener(LISTENER);
        }
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener LISTENER) {
        propertySupport.removePropertyChangeListener(LISTENER);
    }

    /**
    * Calculates the area that is available for painting the display
    */
    private void calcInnerBounds() {
        final Insets INSETS = getInsets();
        INNER_BOUNDS.setBounds(INSETS.left, INSETS.top, getWidth() - INSETS.left - INSETS.right, getHeight() - INSETS.top - INSETS.bottom);
    }

    /**
     * Returns a rectangle representing the available space for drawing the
     * component taking the insets into account (e.g. given through borders etc.)
     * @return a rectangle that represents the area available for rendering the component
     */
    private Rectangle getInnerBounds() {
        return INNER_BOUNDS;
    }

    @Override
    public Dimension getMinimumSize() {
        /* Return the default size of the component
         * which will be used by ui-editors for initialization
         */
        return new Dimension(99, 113);
    }

	@Override
	public void setPreferredSize(final Dimension DIM) {
	    final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
	    if (square) {
	        super.setPreferredSize(new Dimension(SIZE, SIZE));
	    } else {
	        super.setPreferredSize(DIM);
	    }
	    calcInnerBounds();
	    init(getInnerBounds().width, getInnerBounds().height);
	}

	@Override
	public void setSize(final int WIDTH, final int HEIGHT) {
	    final int SIZE = WIDTH <= HEIGHT ? WIDTH : HEIGHT;
	    if (square) {
	        super.setSize(SIZE, SIZE);
	    } else {
	        super.setSize(WIDTH, HEIGHT);
	    }
	    calcInnerBounds();
	    init(getInnerBounds().width, getInnerBounds().height);
	}

	@Override
	public void setSize(final Dimension DIM) {
	    final int SIZE = DIM.width <= DIM.height ? DIM.width : DIM.height;
	    if (square) {
	        super.setSize(new Dimension(SIZE, SIZE));
	    } else {
	        super.setSize(DIM);
	    }
	    calcInnerBounds();
	    init(getInnerBounds().width, getInnerBounds().height);
	}

	@Override
	public void setBounds(final Rectangle BOUNDS) {
	    final int SIZE = BOUNDS.width <= BOUNDS.height ? BOUNDS.width : BOUNDS.height;
	    if (square) {
	        super.setBounds(BOUNDS.x, BOUNDS.y, SIZE, SIZE);
	    } else {
	        super.setBounds(BOUNDS);
	    }
	    calcInnerBounds();
	    init(getInnerBounds().width, getInnerBounds().height);
	}

	@Override
	public void setBounds(final int X, final int Y, final int WIDTH, final int HEIGHT) {
	    final int SIZE = WIDTH <= HEIGHT ? WIDTH : HEIGHT;
	    if (square) {
	        super.setBounds(X, Y, SIZE, SIZE);
	    } else {
	        super.setBounds(X, Y, WIDTH, HEIGHT);
	    }
	    calcInnerBounds();
	    init(getInnerBounds().width, getInnerBounds().height);
	}

    /**
     * Returns a compatible image of the given size and transparency
     * @param WIDTH
     * @param HEIGHT
     * @param TRANSPARENCY
     * @return a compatible image of the given size and transparency
     */
    private BufferedImage createImage(final int WIDTH, final int HEIGHT, final int TRANSPARENCY) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, TRANSPARENCY);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, TRANSPARENCY);
        return IMAGE;
    }

	// Image methods
    public BufferedImage createOffImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final GeneralPath GLAS = new GeneralPath();
        GLAS.setWindingRule(Path2D.WIND_EVEN_ODD);
        GLAS.moveTo(0.2894736842105263 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.curveTo(0.2894736842105263 * IMAGE_WIDTH, 0.5614035087719298 * IMAGE_HEIGHT, 0.38596491228070173 * IMAGE_WIDTH, 0.6052631578947368 * IMAGE_HEIGHT, 0.38596491228070173 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        GLAS.curveTo(0.38596491228070173 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.5877192982456141 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.5877192982456141 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        GLAS.curveTo(0.5877192982456141 * IMAGE_WIDTH, 0.6052631578947368 * IMAGE_HEIGHT, 0.6929824561403509 * IMAGE_WIDTH, 0.5614035087719298 * IMAGE_HEIGHT, 0.6929824561403509 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.curveTo(0.6929824561403509 * IMAGE_WIDTH, 0.32456140350877194 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT);
        GLAS.curveTo(0.38596491228070173 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT, 0.2894736842105263 * IMAGE_WIDTH, 0.32456140350877194 * IMAGE_HEIGHT, 0.2894736842105263 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.closePath();
        final LinearGradientPaint GLAS_PAINT = new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.2894736842105263 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.7017543859649122 * IMAGE_HEIGHT), new float[]{0.0f, 0.99f, 1.0f}, new Color[]{new Color(0.9333333333f, 0.9333333333f, 0.9333333333f, 1f), new Color(0.6f, 0.6f, 0.6f, 1f), new Color(0.6f, 0.6f, 0.6f, 1f)});
        G2.setPaint(GLAS_PAINT);
        G2.fill(GLAS);
        G2.setPaint(new Color(0.8f, 0.8f, 0.8f, 1f));
        G2.setStroke(new BasicStroke((0.010101010101010102f * IMAGE_WIDTH), 0, 1));
        G2.draw(GLAS);
        G2.drawImage(JavaShadow.INSTANCE.createInnerShadow((Shape) GLAS, GLAS_PAINT, 0, 0.35f, new Color(0, 0, 0, 50), (int) 10.0, 45), GLAS.getBounds().x, GLAS.getBounds().y, null);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createOnImage(final int WIDTH, final int HEIGHT, final Color GLOW_COLOR) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final GeneralPath GLOW = new GeneralPath();
        GLOW.setWindingRule(Path2D.WIND_EVEN_ODD);
        GLOW.moveTo(0.05263157894736842 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLOW.curveTo(0.05263157894736842 * IMAGE_WIDTH, 0.19298245614035087 * IMAGE_HEIGHT, 0.24561403508771928 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT, 0.49122807017543857 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT);
        GLOW.curveTo(0.7368421052631579 * IMAGE_WIDTH, 0.0 * IMAGE_HEIGHT, 0.9298245614035088 * IMAGE_WIDTH, 0.19298245614035087 * IMAGE_HEIGHT, 0.9298245614035088 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLOW.curveTo(0.9298245614035088 * IMAGE_WIDTH, 0.6842105263157895 * IMAGE_HEIGHT, 0.7368421052631579 * IMAGE_WIDTH, 0.8771929824561403 * IMAGE_HEIGHT, 0.49122807017543857 * IMAGE_WIDTH, 0.8771929824561403 * IMAGE_HEIGHT);
        GLOW.curveTo(0.24561403508771928 * IMAGE_WIDTH, 0.8771929824561403 * IMAGE_HEIGHT, 0.05263157894736842 * IMAGE_WIDTH, 0.6842105263157895 * IMAGE_HEIGHT, 0.05263157894736842 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLOW.closePath();
        G2.setPaint(new RadialGradientPaint(new Point2D.Double(0.4824561403508772 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT), (0.44298245614035087f * IMAGE_WIDTH), new float[]{0.0f, 1.0f}, new Color[]{new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 255), new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0)}));
        G2.fill(GLOW);

        final GeneralPath GLAS = new GeneralPath();
        GLAS.setWindingRule(Path2D.WIND_EVEN_ODD);
        GLAS.moveTo(0.2894736842105263 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.curveTo(0.2894736842105263 * IMAGE_WIDTH, 0.5614035087719298 * IMAGE_HEIGHT, 0.38596491228070173 * IMAGE_WIDTH, 0.6052631578947368 * IMAGE_HEIGHT, 0.38596491228070173 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        GLAS.curveTo(0.38596491228070173 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.5877192982456141 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.5877192982456141 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        GLAS.curveTo(0.5877192982456141 * IMAGE_WIDTH, 0.6052631578947368 * IMAGE_HEIGHT, 0.6929824561403509 * IMAGE_WIDTH, 0.5614035087719298 * IMAGE_HEIGHT, 0.6929824561403509 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.curveTo(0.6929824561403509 * IMAGE_WIDTH, 0.32456140350877194 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT);
        GLAS.curveTo(0.38596491228070173 * IMAGE_WIDTH, 0.22807017543859648 * IMAGE_HEIGHT, 0.2894736842105263 * IMAGE_WIDTH, 0.32456140350877194 * IMAGE_HEIGHT, 0.2894736842105263 * IMAGE_WIDTH, 0.43859649122807015 * IMAGE_HEIGHT);
        GLAS.closePath();
        final float[] HSB = Color.RGBtoHSB(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(), null);
        final Color[] GLOW_COLORS;
        if (glowColor.getRed() == glowColor.getGreen() && glowColor.getGreen() == glowColor.getBlue()) {
            GLOW_COLORS = new Color[]{
                new Color(Color.HSBtoRGB(0.0f, 0.0f, 0.6f)),
                new Color(Color.HSBtoRGB(0.0f, 0.0f, 0.4f))
            };
        } else {
            GLOW_COLORS = new Color[]{
                new Color(Color.HSBtoRGB(HSB[0], 0.6f, HSB[2])),
                new Color(Color.HSBtoRGB(HSB[0], 0.4f, HSB[2]))
            };
        }
        final LinearGradientPaint GLAS_PAINT = new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.2894736842105263 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.7017543859649122 * IMAGE_HEIGHT), new float[]{0.0f, 1.0f}, GLOW_COLORS);
        G2.setPaint(GLAS_PAINT);
        G2.fill(GLAS);
        G2.setPaint(GLOW_COLOR);
        G2.setStroke(new BasicStroke((0.010101010101010102f * IMAGE_WIDTH), 0, 1));
        G2.draw(GLAS);

        G2.dispose();
        return IMAGE;
    }

    public BufferedImage createBulbImage(final int WIDTH, final int HEIGHT) {
        final GraphicsConfiguration GFX_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (WIDTH <= 0 || HEIGHT <= 0) {
            return GFX_CONF.createCompatibleImage(1, 1, java.awt.Transparency.TRANSLUCENT);
        }
        final BufferedImage IMAGE = GFX_CONF.createCompatibleImage(WIDTH, HEIGHT, Transparency.TRANSLUCENT);
        final Graphics2D G2 = IMAGE.createGraphics();
        G2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        G2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        G2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        G2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int IMAGE_WIDTH = IMAGE.getWidth();
        final int IMAGE_HEIGHT = IMAGE.getHeight();
        final GeneralPath HIGHLIGHT = new GeneralPath();
        HIGHLIGHT.setWindingRule(Path2D.WIND_EVEN_ODD);
        HIGHLIGHT.moveTo(0.3508771929824561 * IMAGE_WIDTH, 0.3333333333333333 * IMAGE_HEIGHT);
        HIGHLIGHT.curveTo(0.3508771929824561 * IMAGE_WIDTH, 0.2807017543859649 * IMAGE_HEIGHT, 0.41228070175438597 * IMAGE_WIDTH, 0.23684210526315788 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.23684210526315788 * IMAGE_HEIGHT);
        HIGHLIGHT.curveTo(0.5789473684210527 * IMAGE_WIDTH, 0.23684210526315788 * IMAGE_HEIGHT, 0.6403508771929824 * IMAGE_WIDTH, 0.2807017543859649 * IMAGE_HEIGHT, 0.6403508771929824 * IMAGE_WIDTH, 0.3333333333333333 * IMAGE_HEIGHT);
        HIGHLIGHT.curveTo(0.6403508771929824 * IMAGE_WIDTH, 0.38596491228070173 * IMAGE_HEIGHT, 0.5789473684210527 * IMAGE_WIDTH, 0.4298245614035088 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.4298245614035088 * IMAGE_HEIGHT);
        HIGHLIGHT.curveTo(0.41228070175438597 * IMAGE_WIDTH, 0.4298245614035088 * IMAGE_HEIGHT, 0.3508771929824561 * IMAGE_WIDTH, 0.38596491228070173 * IMAGE_HEIGHT, 0.3508771929824561 * IMAGE_WIDTH, 0.3333333333333333 * IMAGE_HEIGHT);
        HIGHLIGHT.closePath();
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.24561403508771928 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 0.4298245614035088 * IMAGE_HEIGHT), new float[]{0.0f, 0.99f, 1.0f}, new Color[]{new Color(1f, 1f, 1f, 1f), new Color(1f, 1f, 1f, 0f), new Color(1f, 1f, 1f, 0f)}));
        G2.fill(HIGHLIGHT);

        final GeneralPath WINDING = new GeneralPath();
        WINDING.setWindingRule(Path2D.WIND_EVEN_ODD);
        WINDING.moveTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        WINDING.curveTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.4298245614035088 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT, 0.49122807017543857 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT);
        WINDING.curveTo(0.5614035087719298 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.7368421052631579 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.7368421052631579 * IMAGE_HEIGHT);
        WINDING.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.7631578947368421 * IMAGE_HEIGHT);
        WINDING.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.7807017543859649 * IMAGE_HEIGHT);
        WINDING.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.7982456140350878 * IMAGE_HEIGHT);
        WINDING.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8157894736842105 * IMAGE_HEIGHT);
        WINDING.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.8333333333333334 * IMAGE_HEIGHT);
        WINDING.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8508771929824561 * IMAGE_HEIGHT);
        WINDING.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.868421052631579 * IMAGE_HEIGHT);
        WINDING.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8859649122807017 * IMAGE_HEIGHT);
        WINDING.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT);
        WINDING.curveTo(0.6052631578947368 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT, 0.5701754385964912 * IMAGE_WIDTH, 0.956140350877193 * IMAGE_HEIGHT, 0.5350877192982456 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        WINDING.curveTo(0.5263157894736842 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT, 0.5175438596491229 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT);
        WINDING.curveTo(0.4824561403508772 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.47368421052631576 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.4649122807017544 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        WINDING.curveTo(0.42105263157894735 * IMAGE_WIDTH, 0.9473684210526315 * IMAGE_HEIGHT, 0.39473684210526316 * IMAGE_WIDTH, 0.9035087719298246 * IMAGE_HEIGHT, 0.39473684210526316 * IMAGE_WIDTH, 0.9035087719298246 * IMAGE_HEIGHT);
        WINDING.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT);
        WINDING.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8859649122807017 * IMAGE_HEIGHT);
        WINDING.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.868421052631579 * IMAGE_HEIGHT);
        WINDING.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8508771929824561 * IMAGE_HEIGHT);
        WINDING.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.8333333333333334 * IMAGE_HEIGHT);
        WINDING.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8157894736842105 * IMAGE_HEIGHT);
        WINDING.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.7982456140350878 * IMAGE_HEIGHT);
        WINDING.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7894736842105263 * IMAGE_HEIGHT);
        WINDING.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.7719298245614035 * IMAGE_HEIGHT);
        WINDING.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7631578947368421 * IMAGE_HEIGHT);
        WINDING.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        WINDING.closePath();
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.47368421052631576 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT), new Point2D.Double(0.4847023065774619 * IMAGE_WIDTH, 0.9383079722290332 * IMAGE_HEIGHT), new float[]{0.0f, 0.04f, 0.19f, 0.24f, 0.31f, 0.4f, 0.48f, 0.56f, 0.64f, 0.7f, 0.78f, 1.0f}, new Color[]{new Color(0.2f, 0.2f, 0.2f, 1f), new Color(0.8509803922f, 0.8470588235f, 0.8392156863f, 1f), new Color(0.8941176471f, 0.8980392157f, 0.8784313725f, 1f), new Color(0.5921568627f, 0.6f, 0.5882352941f, 1f), new Color(0.9843137255f, 1f, 1f, 1f), new Color(0.5058823529f, 0.5215686275f, 0.5176470588f, 1f), new Color(0.9607843137f, 0.9686274510f, 0.9568627451f, 1f), new Color(0.5843137255f, 0.5921568627f, 0.5803921569f, 1f), new Color(0.9490196078f, 0.9490196078f, 0.9411764706f, 1f), new Color(0.5098039216f, 0.5294117647f, 0.5137254902f, 1f), new Color(0.9882352941f, 0.9882352941f, 0.9882352941f, 1f), new Color(0.4f, 0.4f, 0.4f, 1f)}));
        G2.fill(WINDING);

        final GeneralPath WINDING_SHADOW = new GeneralPath();
        WINDING_SHADOW.setWindingRule(Path2D.WIND_EVEN_ODD);
        WINDING_SHADOW.moveTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT, 0.4298245614035088 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT, 0.49122807017543857 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.5614035087719298 * IMAGE_WIDTH, 0.7280701754385965 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.7368421052631579 * IMAGE_HEIGHT, 0.6052631578947368 * IMAGE_WIDTH, 0.7368421052631579 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.7631578947368421 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.7807017543859649 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.7982456140350878 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8157894736842105 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.8333333333333334 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8508771929824561 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.868421052631579 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.5964912280701754 * IMAGE_WIDTH, 0.8859649122807017 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.6052631578947368 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.6052631578947368 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT, 0.5701754385964912 * IMAGE_WIDTH, 0.956140350877193 * IMAGE_HEIGHT, 0.5350877192982456 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.5263157894736842 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT, 0.5175438596491229 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.4824561403508772 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.47368421052631576 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.4649122807017544 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        WINDING_SHADOW.curveTo(0.42105263157894735 * IMAGE_WIDTH, 0.9473684210526315 * IMAGE_HEIGHT, 0.39473684210526316 * IMAGE_WIDTH, 0.9035087719298246 * IMAGE_HEIGHT, 0.39473684210526316 * IMAGE_WIDTH, 0.9035087719298246 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.8947368421052632 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8859649122807017 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.868421052631579 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8508771929824561 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.8333333333333334 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.38596491228070173 * IMAGE_WIDTH, 0.8157894736842105 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.7982456140350878 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7894736842105263 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.39473684210526316 * IMAGE_WIDTH, 0.7719298245614035 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7631578947368421 * IMAGE_HEIGHT);
        WINDING_SHADOW.lineTo(0.37719298245614036 * IMAGE_WIDTH, 0.7456140350877193 * IMAGE_HEIGHT);
        WINDING_SHADOW.closePath();
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.37719298245614036 * IMAGE_WIDTH, 0.7894736842105263 * IMAGE_HEIGHT), new Point2D.Double(0.6052631578947368 * IMAGE_WIDTH, 0.7894736842105263 * IMAGE_HEIGHT), new float[]{0.0f, 0.15f, 0.85f, 1.0f}, new Color[]{new Color(0f, 0f, 0f, 0.4f), new Color(0f, 0f, 0f, 0.0f), new Color(0f, 0f, 0f, 0.0f), new Color(0f, 0f, 0f, 0.4f)}));
        G2.fill(WINDING_SHADOW);

        final GeneralPath CONTACT_PLATE = new GeneralPath();
        CONTACT_PLATE.setWindingRule(Path2D.WIND_EVEN_ODD);
        CONTACT_PLATE.moveTo(0.42105263157894735 * IMAGE_WIDTH, 0.9473684210526315 * IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.43859649122807015 * IMAGE_WIDTH, 0.956140350877193 * IMAGE_HEIGHT, 0.4473684210526316 * IMAGE_WIDTH, 0.9736842105263158 * IMAGE_HEIGHT, 0.4649122807017544 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.47368421052631576 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.4824561403508772 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.5175438596491229 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT, 0.5263157894736842 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT, 0.5350877192982456 * IMAGE_WIDTH, 0.9912280701754386 * IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.543859649122807 * IMAGE_WIDTH, 0.9824561403508771 * IMAGE_HEIGHT, 0.5614035087719298 * IMAGE_WIDTH, 0.956140350877193 * IMAGE_HEIGHT, 0.5789473684210527 * IMAGE_WIDTH, 0.9473684210526315 * IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.5526315789473685 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT, 0.5263157894736842 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT, 0.5 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT);
        CONTACT_PLATE.curveTo(0.47368421052631576 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT, 0.4473684210526316 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT, 0.42105263157894735 * IMAGE_WIDTH, 0.9473684210526315 * IMAGE_HEIGHT);
        CONTACT_PLATE.closePath();
        G2.setPaint(new LinearGradientPaint(new Point2D.Double(0.5 * IMAGE_WIDTH, 0.9385964912280702 * IMAGE_HEIGHT), new Point2D.Double(0.5 * IMAGE_WIDTH, 1.0 * IMAGE_HEIGHT), new float[]{0.0f, 0.61f, 0.71f, 0.83f, 1.0f}, new Color[]{new Color(0.0196078431f, 0.0392156863f, 0.0235294118f, 1f), new Color(0.0274509804f, 0.0235294118f, 0.0078431373f, 1f), new Color(0.6f, 0.5725490196f, 0.5333333333f, 1f), new Color(0.0039215686f, 0.0039215686f, 0.0039215686f, 1f), new Color(0f, 0f, 0f, 1f)}));
        G2.fill(CONTACT_PLATE);

        G2.dispose();
        return IMAGE;
    }

	@Override
	public String toString() {
		return "LightBulb";
	}
}

