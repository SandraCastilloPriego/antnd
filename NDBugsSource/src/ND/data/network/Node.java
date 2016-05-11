/*
 * Copyright 2013-2014 VTT Biotechnology
 * This file is part of AntND.
 *
 * AntND is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * AntND is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * AntND; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.data.network;

import java.awt.Color;
import java.awt.geom.Point2D;

/**
 *
 * @author scsandra
 */
public class Node {

    private String id;
    private String name;
    private Point2D position;
    private Color color;

    public Node(String id) {
        this.id = id;
        this.color = null;
    }

    public Node(String id, String name) {
        this.id = id;
        this.name = name;
        this.color = null;
    }

    public String getId() {
        return id;
    }

    public String getCompleteId() {
        return this.id + " : " + this.getName();
    }

    public String getName() {
        return name;
    }

    public void setId(String newID) {
        this.id = newID;
    }

    @Override
    public Node clone() {
        Node n = new Node(this.id, this.name);
        n.position = this.position;
        return n;
    }

    public void setPosition(double x, double y) {
        this.position = new Point2D.Double(x, y);
    }

    public void setPosition(Point2D position) {
        if (position != null) {
            this.position = (Point2D) position.clone();
        }
    }

    public Point2D getPosition() {
        return this.position;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }
}
