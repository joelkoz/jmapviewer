package org.openstreetmap.gui.jmapviewer;

//License: GPL.

import static org.openstreetmap.gui.jmapviewer.FeatureAdapter.tr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.HashMap;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public class AttributionSupport {

    private TileSource tileSource;

    private Image attrImage;
    private String attrTermsText;
    private String attrTermsUrl;
    public static final Font ATTR_FONT = new Font("Arial", Font.PLAIN, 10);
    public static final Font ATTR_LINK_FONT;
    
    private static final String DEFAULT_TERMS_OF_USE_TEXT = tr("Background Terms of Use");

    protected Rectangle attrTextBounds = null;
    protected Rectangle attrToUBounds = null;
    protected Rectangle attrImageBounds = null;

    static {
        HashMap<TextAttribute, Integer> aUnderline = new HashMap<TextAttribute, Integer>();
        aUnderline.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        ATTR_LINK_FONT = ATTR_FONT.deriveFont(aUnderline);
    }

    public void initialize(TileSource tileSource) {
        this.tileSource = tileSource;
        boolean requireAttr = tileSource.requiresAttribution();
        if (requireAttr) {
            attrImage = tileSource.getAttributionImage();
            attrTermsText = tileSource.getTermsOfUseText();
            attrTermsUrl = tileSource.getTermsOfUseURL();
            if (attrTermsUrl != null && attrTermsText == null) {
                attrTermsText = DEFAULT_TERMS_OF_USE_TEXT;
            }
        } else {
            attrImage = null;
            attrTermsUrl = null;
        }
    }

    public void paintAttribution(Graphics g, int width, int height, Coordinate topLeft, Coordinate bottomRight, int zoom, ImageObserver observer) {
        if (tileSource == null || !tileSource.requiresAttribution())
            return;
        // Draw attribution
        Font font = g.getFont();
        g.setFont(ATTR_LINK_FONT);

        // Draw terms of use text
        int termsTextHeight = 0;
        int termsTextY = height;

        if (attrTermsText != null) {
            Rectangle2D termsStringBounds = g.getFontMetrics().getStringBounds(attrTermsText, g);
            int textRealHeight = (int) termsStringBounds.getHeight();
            termsTextHeight = textRealHeight - 5;
            int termsTextWidth = (int) termsStringBounds.getWidth();
            termsTextY = height - termsTextHeight;
            int x = 2;
            int y = height - termsTextHeight;
            attrToUBounds = new Rectangle(x, y-termsTextHeight, termsTextWidth, textRealHeight);
            g.setColor(Color.black);
            g.drawString(attrTermsText, x + 1, y + 1);
            g.setColor(Color.white);
            g.drawString(attrTermsText, x, y);
        }

        // Draw attribution logo
        if (attrImage != null) {
            int x = 2;
            int imgWidth = attrImage.getWidth(observer);
            int imgHeight = attrImage.getHeight(observer);
            int y = termsTextY - imgHeight - termsTextHeight - 5;
            attrImageBounds = new Rectangle(x, y, imgWidth, imgHeight);
            g.drawImage(attrImage, x, y, null);
        }

        g.setFont(ATTR_FONT);
        String attributionText = tileSource.getAttributionText(zoom, topLeft, bottomRight);
        if (attributionText != null) {
            Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(attributionText, g);
            int textHeight = (int) stringBounds.getHeight() - 5;
            int x = width - (int) stringBounds.getWidth();
            int y = height - textHeight;
            g.setColor(Color.black);
            g.drawString(attributionText, x + 1, y + 1);
            g.setColor(Color.white);
            g.drawString(attributionText, x, y);
            attrTextBounds = new Rectangle(x, y-textHeight, (int) stringBounds.getWidth(), (int) stringBounds.getHeight());
        }

        g.setFont(font);
    }

    public boolean handleAttribution(Point p, boolean click) {
        if (tileSource == null || !tileSource.requiresAttribution())
            return false;

        /* TODO: Somehow indicate the link is clickable state to user */

        if (attrTextBounds != null && attrTextBounds.contains(p)) {
            String attributionURL = tileSource.getAttributionLinkURL();
            if (attributionURL != null) {
                if (click) {
                    FeatureAdapter.openLink(attributionURL);
                }
                return true;
            }
        } else if (attrImageBounds != null && attrImageBounds.contains(p)) {
            String attributionImageURL = tileSource.getAttributionImageURL();
            if (attributionImageURL != null) {
                if (click) {
                    FeatureAdapter.openLink(tileSource.getAttributionImageURL());
                }
                return true;
            }
        } else if (attrToUBounds != null && attrToUBounds.contains(p)) {
            String termsOfUseURL = tileSource.getTermsOfUseURL();
            if (termsOfUseURL != null) {
                if (click) {
                    FeatureAdapter.openLink(termsOfUseURL);
                }
                return true;
            }
        }
        return false;
    }

}

