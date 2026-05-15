package com.example.projectpoker.model.game;

import java.util.List;

public class TablePosition {
    public double x;
    public double y;
    public double spacingX;
    public double spacingY;
    public double rotation;
    public double vScale;
    public double nameplateOffsetX;
    public double nameplateOffsetY;
    public double chipOffsetX;
    public double chipOffsetY;

    public TablePosition(double x, double y, double spacingX, double spacingY, double rotation) {
        this(x, y, spacingX, spacingY, rotation, 0.3, 0, 0, 0, 0);
    }

    public TablePosition(double x, double y, double spacingX, double spacingY, double rotation, double vScale) {
        this(x, y, spacingX, spacingY, rotation, vScale, 0, 0, 0, 0);
    }

    public TablePosition(double x, double y, double spacingX, double spacingY, double rotation, double vScale, double nameplateOffsetX, double nameplateOffsetY) {
        this(x, y, spacingX, spacingY, rotation, vScale, nameplateOffsetX, nameplateOffsetY, 0, 0);
    }

    public TablePosition(double x, double y, double spacingX, double spacingY, double rotation, double vScale, double nameplateOffsetX, double nameplateOffsetY, double chipOffsetX, double chipOffsetY) {
        this.x = x;
        this.y = y;
        this.spacingX = spacingX;
        this.spacingY = spacingY;
        this.rotation = rotation;
        this.vScale = vScale;
        this.nameplateOffsetX = nameplateOffsetX;
        this.nameplateOffsetY = nameplateOffsetY;
        this.chipOffsetX = chipOffsetX;
        this.chipOffsetY = chipOffsetY;
    }

    public static TablePosition DeckPos     = new TablePosition(150,85,0,2,90);
    public static TablePosition FoldedPos   = new TablePosition(150,150,0,0,90);
    public static TablePosition PlayerPos   = new TablePosition(300,300,15,0,0, 0.3, -80, -10, 60, -40);
    public static TablePosition BoardPos    = new TablePosition(250,123,54,0,0, 1);

    public static TablePosition TopLeftPos   = new TablePosition(190, 0, -15, 0, 180, 0.3, -20, 25, 70, 10);
    public static TablePosition TopMidPos    = new TablePosition(380, 0, -15, 0, 180, 0.3, -20, 25, 70, 10);
    public static TablePosition TopRightPos  = new TablePosition(580, 0, -15, 0, 180, 0.3, -20, 25, 70, 10);

    public static TablePosition LeftPos      = new TablePosition(-15, 140, 0, 15, 90, 0.3, 20, 60, 40, 10);
    public static TablePosition RightPos     = new TablePosition(765, 140, 0, -15, -90, 0.3, -40, 40, -80, -15);

    public static TablePosition PotPos       = new TablePosition(550, 110, 0, 0, 0, 120);

    public static List<TablePosition> PosList = List.of(PlayerPos, LeftPos, TopLeftPos, TopMidPos, TopRightPos, RightPos);

}