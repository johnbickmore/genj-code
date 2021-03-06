/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2009 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.geom;

import java.awt.geom.Point2D;

/**
 * Interface to implementation that knows how to handle line-segments line,quad-curve,cubic-curve
 */
public interface PathConsumer {

  
  /**
   * Callback - line
   */
  public boolean consumeLine(Point2D start, Point2D end);
  
  /**
   * Callback - quad curve
   */
  public boolean consumeQuadCurve(Point2D start, Point2D ctrl, Point2D end);
  
  /**
   * Callback - cubic curve
   */
  public boolean consumeCubicCurve(Point2D start, Point2D ctrl1, Point2D ctrl2, Point2D end);
  
}
