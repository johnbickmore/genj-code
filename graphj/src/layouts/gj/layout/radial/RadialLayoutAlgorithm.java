/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2002-2004 Nils Meier
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
package gj.layout.radial;

import gj.geom.Geometry;
import gj.layout.AbstractLayoutAlgorithm;
import gj.layout.GraphNotSupportedException;
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.model.Graph;
import gj.model.Tree;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A radial layout for Trees
 */
public class RadialLayoutAlgorithm extends AbstractLayoutAlgorithm implements LayoutAlgorithm {
  
  private Object rootOfTree;
  private double distanceBetweenGenerations = 60;
  private boolean isAdjustDistances = false;
  private boolean isFanOut = false; 
  private double distanceInGeneration = 0;
  private boolean isOrderSiblingsByPosition = true;

  /**
   * Accessor - root node
   */
  public Object getRootOfTree() {
    return rootOfTree;
  }

  /**
   * Accessor - root node
   */
  public void setRootOfTree(Object root) {
    this.rootOfTree = root;
  }
  
  /**
   * Accessor - distance of generations
   */
  public void setDistanceBetweenGenerations(double distanceBetweenGenerations) {
    this.distanceBetweenGenerations = distanceBetweenGenerations;
  }

  /**
   * Accessor - distance of generations
   */
  public double getDistanceBetweenGenerations() {
    return distanceBetweenGenerations;
  }

  /**
   * Accessor - distance in generation
   */
  public void setDistanceInGeneration(double distanceInGeneration) {
    this.distanceInGeneration = distanceInGeneration;
  }

  /**
   * Accessor - distance in generation
   */
  public double getDistanceInGeneration() {
    return distanceInGeneration;
  }

  /**
   * Accessor - whether to adjust distances to generate space avoiding vertex overlap 
   */
  public boolean isAdjustDistances() {
    return isAdjustDistances;
  }

  /**
   * Accessor - whether to adjust distances to generate space avoiding vertex overlap 
   */
  public void setAdjustDistances(boolean isAdjustDistances) {
    this.isAdjustDistances = isAdjustDistances;
  }

  /**
   * Accessor - whether to fan out children as much as possible or group them closely
   */
  public boolean isFanOut() {
    return isFanOut;
  }

  /**
   * Accessor - whether to fan out children as much as possible or group them closely
   */
  public void setFanOut(boolean isFanOut) {
    this.isFanOut = isFanOut;
  }

  /**
   * Setter - whether to order siblings by their current position
   */
  public void setOrderSiblingsByPosition(boolean isOrderSiblingsByPosition) {
    this.isOrderSiblingsByPosition = isOrderSiblingsByPosition;
  }

  /**
   * Getter - whether to order siblings by their current position
   */
  public boolean isOrderSiblingsByPosition() {
    return isOrderSiblingsByPosition;
  }

  /**
   * Layout a layout capable graph
   */
  public Shape apply(Graph graph, Layout2D layout, Rectangle2D bounds, Collection<Shape> debugShapes) throws LayoutAlgorithmException {
    
    // check that we got a tree
    if (!(graph instanceof Tree))
      throw new GraphNotSupportedException("only trees allowed", Tree.class);
    Tree tree = (Tree)graph;
    
    // ignore an empty tree
    Set<?> verticies = tree.getVertices();
    if (verticies.isEmpty())
      return bounds;
    
    // check root
    if (rootOfTree==null || !verticies.contains(rootOfTree)) 
      rootOfTree = verticies.iterator().next();
    
    // calculate sub-tree sizes
    Map<Object, Double> root2size = new HashMap<Object, Double>();
    calcSize(tree, null, rootOfTree, 0, root2size, layout);

    // layout
    Point2D center = layout.getPositionOfVertex(tree, rootOfTree);
    int depth = layout(tree, null, rootOfTree, center, 0, Geometry.ONE_RADIAN, 0, root2size, layout, debugShapes);
    
    // add debug rings
    if (debugShapes!=null) {
      for (int i=0;i<=depth;i++) 
        debugShapes.add(new Ellipse2D.Double(center.getX()-distanceBetweenGenerations*i, center.getY()-distanceBetweenGenerations*i, distanceBetweenGenerations*2*i, distanceBetweenGenerations*2*i));
    }
    
    // done
    return ModelHelper.getBounds(graph, layout);
  }
  
  /**
   * calculate the diameter of a node's shape
   */
  private double getDiameter(Tree tree, Object vertex, Layout2D layout) {
    return Geometry.getMaximumDistance(new Point2D.Double(0,0), layout.getShapeOfVertex(tree, vertex)) * 2;
  }

  
  /**
   * calculate the size of a sub-tree starting at root
   */
  private double calcSize(Tree tree, Object backtrack, Object root, int generation, Map<Object, Double> root2size, Layout2D layout) {

    // calculate size for children
    Set<?> neighbours = tree.getNeighbours(root);
    double sizeOfChildren = 0;
    for (Object child : neighbours) {
      if (!child.equals(backtrack)) {
        sizeOfChildren +=  calcSize(tree, root, child, generation + 1, root2size, layout) * generation/(generation+1);
      }
    }
    
    // calculate size root
    double sizeOfRoot = 0;
    if (backtrack!=null) {
      // radian = diameter / (2PI*r) * 2PI
      sizeOfRoot = getDiameter(tree, root, layout) + distanceInGeneration;
    }
    
    // keep and return
    double result = Math.max( sizeOfChildren, sizeOfRoot);
    root2size.put(root, new Double(result));
    return result;
  }
  
  /**
   * recursive layout call
   */
  private int layout(Graph tree, Object backtrack, Object root, Point2D center, double fromRadian, double toRadian, double radius, Map<Object, Double> root2share, Layout2D layout, Collection<Shape> debugShapes) {
    
    // assemble list of children
    Set<?> neighbours = tree.getNeighbours(root);
    List<Object> children = new ArrayList<Object>(neighbours.size());
    for (Object child : neighbours) 
      if (!child.equals(backtrack)) children.add(child);
    if (children.isEmpty())
      return 1;
    
    // sort children by current position
    if (isOrderSiblingsByPosition) 
      Collections.sort(children, new ComparePositions(tree, center, fromRadian+(toRadian-fromRadian)/2+Geometry.HALF_RADIAN, layout));
    
    // compare actual space vs allocation
    double sizeOfChildren = 0;
    for (Object child : children) {
      sizeOfChildren += root2share.get(child).doubleValue();
    }
    
    radius += distanceBetweenGenerations;
    
    double shareFactor = (toRadian-fromRadian) * radius / sizeOfChildren;
    
    if (shareFactor<0.99 && isAdjustDistances) {
      System.out.println("TODO:adjusting "+root+"'s distance ("+shareFactor);
    } if (shareFactor>1 && !isFanOut) {  
      if (backtrack!=null)
        fromRadian += (toRadian-fromRadian)/shareFactor/2;
      shareFactor = 1;
    }
    
    // position and recurse
    int depth = 0;
    for (Object child : children) {
      
      double radians = root2share.get(child).doubleValue() / radius * shareFactor;
      
      layout.setPositionOfVertex(tree, child, getPoint(center, fromRadian + radians/2, radius));
      
      if (debugShapes!=null) {
        debugShapes.add(new Line2D.Double(getPoint(center, fromRadian, radius), getPoint(center, fromRadian, radius+distanceBetweenGenerations)));
        debugShapes.add(new Line2D.Double(getPoint(center, fromRadian+radians, radius), getPoint(center, fromRadian+radians, radius+distanceBetweenGenerations)));
      }
      
      depth = Math.max(depth, layout(tree, root, child, center, fromRadian, fromRadian+radians, radius, root2share, layout, debugShapes));
      
      fromRadian += radians;
    }

    // done
    return depth + 1;
  }
  
  private Point2D getPoint(Point2D center, double radian, double radius) {
    return new Point2D.Double(
      center.getX() + Math.sin(radian) * (radius),
      center.getY() - Math.cos(radian) * (radius)
      );
  }
  
  /**
   * A comparator for comparing sibling vertices by their position
   */
  private class ComparePositions extends Geometry implements Comparator<Object> {

    private Layout2D layout;
    private Graph graph;
    private Point2D center;
    private double north;
    
    ComparePositions(Graph graph, Point2D center, double north, Layout2D layout) {
      this.graph = graph;
      this.layout = layout;
      this.center = center;
      this.north = north;
    }
    
    public int compare(Object v1,Object v2) {
      
      double r1 = Geometry.getRadian(getDelta(center,layout.getPositionOfVertex(graph, v1)));
      double r2 = Geometry.getRadian(getDelta(center,layout.getPositionOfVertex(graph, v2)));
      
      if (r1>north)
        r1 -= ONE_RADIAN;
      if (r2>north)
        r2 -= ONE_RADIAN;
      
      if (r1<r2) 
        return -1;
      if (r1>r2)
        return 1;
      return 0;
    }
    
  } //ComparePositions
  
} //RadialLayoutAlgorithm
