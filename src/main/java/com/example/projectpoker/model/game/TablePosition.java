package com.example.projectpoker.model.game;

import java.util.ArrayList;
import java.util.List;

public class TablePosition {
    public double x;
    public double y;
    public double spacingX;
    public double spacingY;
    public double rotation;
    public double vScale;

    public TablePosition(double x, double y, double spacingX, double spacingY, double rotation) {
        this.x = x;
        this.y = y;
        this.spacingX = spacingX;
        this.spacingY = spacingY;
        this.rotation = rotation;
        this.vScale = 0.3;
    }
    public TablePosition(double x, double y, double spacingX, double spacingY, double rotation, double vScale) {
        this.x = x;
        this.y = y;
        this.spacingX = spacingX;
        this.spacingY = spacingY;
        this.rotation = rotation;
        this.vScale = vScale;
    }

    public static TablePosition DeckPos =   new TablePosition(150,85,0,2,90);
    public static TablePosition FoldedPos = new TablePosition(150,150,0,0,90);
    public static TablePosition PlayerPos = new TablePosition(300,300,15,0,0);
    public static TablePosition BoardPos =  new TablePosition(250,123,54,0,0, 1);

    public static TablePosition TopLeftPos   = new TablePosition(180, 0, -15, 0, 180);
    public static TablePosition TopMidPos    = new TablePosition(330, 0, -15, 0, 180);
    public static TablePosition TopRightPos  = new TablePosition(500, 0, -15, 0, 180);

    public static TablePosition LeftPos      = new TablePosition(-15, 140, 0, 15, 90);
    public static TablePosition RightPos     = new TablePosition(765, 140, 0, -15, -90);

    public static List<TablePosition> PosList = List.of(PlayerPos, LeftPos, TopLeftPos, TopMidPos, TopRightPos, RightPos);

}