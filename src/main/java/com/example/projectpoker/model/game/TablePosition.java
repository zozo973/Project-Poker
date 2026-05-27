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

    /**
     * Creates a table position with default vertical crop scale and no label/chip offsets.
     *
     * @param x x-coordinate for the first rendered card
     * @param y y-coordinate for the first rendered card
     * @param spacingX horizontal spacing between cards
     * @param spacingY vertical spacing between cards
     * @param rotation card rotation angle in degrees
     */
    public TablePosition(double x, double y, double spacingX, double spacingY, double rotation) {
        this(x, y, spacingX, spacingY, rotation, 0.3, 0, 0, 0, 0);
    }

    /**
     * Creates a table position with an explicit vertical crop scale and no label/chip offsets.
     *
     * @param x x-coordinate for the first rendered card
     * @param y y-coordinate for the first rendered card
     * @param spacingX horizontal spacing between cards
     * @param spacingY vertical spacing between cards
     * @param rotation card rotation angle in degrees
     * @param vScale visible vertical portion of each card image
     */
    public TablePosition(double x, double y, double spacingX, double spacingY, double rotation, double vScale) {
        this(x, y, spacingX, spacingY, rotation, vScale, 0, 0, 0, 0);
    }

    /**
     * Creates a table position with explicit nameplate offsets and no chip offsets.
     *
     * @param x x-coordinate for the first rendered card
     * @param y y-coordinate for the first rendered card
     * @param spacingX horizontal spacing between cards
     * @param spacingY vertical spacing between cards
     * @param rotation card rotation angle in degrees
     * @param vScale visible vertical portion of each card image
     * @param nameplateOffsetX x-offset for the player's nameplate label
     * @param nameplateOffsetY y-offset for the player's nameplate label
     */
    public TablePosition(double x, double y, double spacingX, double spacingY, double rotation, double vScale, double nameplateOffsetX, double nameplateOffsetY) {
        this(x, y, spacingX, spacingY, rotation, vScale, nameplateOffsetX, nameplateOffsetY, 0, 0);
    }

    /**
     * Creates a fully configured table position for card, label, and chip rendering.
     *
     * @param x x-coordinate for the first rendered card
     * @param y y-coordinate for the first rendered card
     * @param spacingX horizontal spacing between cards
     * @param spacingY vertical spacing between cards
     * @param rotation card rotation angle in degrees
     * @param vScale visible vertical portion of each card image
     * @param nameplateOffsetX x-offset for the player's nameplate label
     * @param nameplateOffsetY y-offset for the player's nameplate label
     * @param chipOffsetX x-offset for the player's chip stack image
     * @param chipOffsetY y-offset for the player's chip stack image
     */
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
    public static TablePosition PlayerPos   = new TablePosition(270,280,20,0,0, 0.41, -80, -30, 60, -50);
    public static TablePosition BoardPos    = new TablePosition(250,123,54,0,0, 1);

    public static TablePosition TopLeftPos   = new TablePosition(190, 0, -15, 0, 180, 0.3, -20, 25, 70, 10);
    public static TablePosition TopMidPos    = new TablePosition(380, 0, -15, 0, 180, 0.3, -20, 25, 70, 10);
    public static TablePosition TopRightPos  = new TablePosition(580, 0, -15, 0, 180, 0.3, -20, 25, 70, 10);

    public static TablePosition LeftPos      = new TablePosition(-15, 140, 0, 15, 90, 0.3, 20, 60, 40, 10);
    public static TablePosition RightPos     = new TablePosition(765, 140, 0, -15, -90, 0.3, -40, 40, -80, -15);

    public static TablePosition PotPos       = new TablePosition(550, 110, 0, 0, 0, 120);

    public static List<TablePosition> PosList = List.of(PlayerPos, LeftPos, TopLeftPos, TopMidPos, TopRightPos, RightPos);

}