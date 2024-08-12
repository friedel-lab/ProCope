/*
 * This file is part of ProCope
 *
 * ProCope is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProCope is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ProCope.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2008, Teaching and Research Unit Bioinformatics, LMU Munich
 * http://www.bio.ifi.lmu.de/Complexes/ProCope/
 *
 */
package procope.userinterface.gui;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.JComponent;
 
/**
 * Multline JLabel class found at
 * http://www.koders.com/java/fid9BE9B31AC9BED01828448BF91A61AFA5AE431E16.aspx 
 *
 */
public class JMultilineLabel extends JComponent {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6891961488325003367L;
	
	private String text;
    private Insets margin = new Insets(5,5,5,5);
    private int maxWidth = Integer.MAX_VALUE;
    private boolean justify;
    private final FontRenderContext frc = new FontRenderContext(null, true, false);
 
    public JMultilineLabel() {
    	
    }
    
    public JMultilineLabel(String text) {
		this.text = text;
	}

	public void setMargin(Insets insets) {
    	this.margin = insets;
    }
    
    private void morph() {
        revalidate();
        repaint();
    }
 
    public String getText() {
        return text;
    }
 
    public void setText(String text) {
        String old = this.text;
        this.text = text;
        firePropertyChange("text", old, this.text);
        if ((old == null) ? text!=null : !old.equals(text))
            morph();
    }
 
    public int getMaxWidth() {
        return maxWidth;
    }
 
    public void setMaxWidth(int maxWidth) {
        if (maxWidth <= 0)
            throw new IllegalArgumentException();
        int old = this.maxWidth;
        this.maxWidth = maxWidth;
        firePropertyChange("maxWidth", old, this.maxWidth);
        if (old !=  this.maxWidth)
            morph();
    }
 
    public boolean isJustified() {
        return justify;
    }
 
    public void setJustified(boolean justify) {
        boolean old = this.justify;
        this.justify = justify;
        firePropertyChange("justified", old, this.justify);
        if (old != this.justify)
            repaint();
    }
 
    public Dimension getPreferredSize() {
        return paintOrGetSize(null, getMaxWidth());
    }
 
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintOrGetSize((Graphics2D)g, getWidth());
    }
 
    private Dimension paintOrGetSize(Graphics2D g, int width) {
    	
//    	if (g!=null)
//    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   // Anti-alias!
//			    RenderingHints.VALUE_ANTIALIAS_ON);
    	
        Insets insets = getInsets();
        width -= insets.left + insets.right + margin.left + margin.right;
        float w = insets.left + insets.right + margin.left + margin.right;
        float x = insets.left + margin.left, y=insets.top + margin.top;
        if (width > 0 && text != null && text.length() > 0) {
            AttributedString as = new AttributedString(getText());
            as.addAttribute(TextAttribute.FONT, getFont());
            AttributedCharacterIterator aci = as.getIterator();
            LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc  );
          
            float max = 0;
            while (lbm.getPosition() < aci.getEndIndex()) {
                TextLayout textLayout = lbm.nextLayout(width);
                if (g != null && isJustified() && textLayout.getVisibleAdvance() > 0.80 * width)
                    textLayout = textLayout.getJustifiedLayout(width);
                if (g != null)
                    textLayout.draw(g, x, y + textLayout.getAscent());
                y += textLayout.getDescent() + textLayout.getLeading() + textLayout.getAscent();
                max = Math.max(max, textLayout.getVisibleAdvance());
            }
         
            w += max;
        }
        return new Dimension((int)Math.ceil(w), (int)Math.ceil(y) + insets.bottom + margin.bottom);
    }
    
}
