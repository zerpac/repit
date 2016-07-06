/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.pdf;

/**
 *
 * @author tc149752
 */
public enum StickerType {


    ZWECKFORM_3481 ("Zweckform", "3481", 3, 7, 70f, 41f, 0f),

    ZWECKFORM_3658 ("Zweckform", "3658", 3, 8, 64.6f, 33.8f, 0f ),

    ZWECKFORM_J4721 ("Zweckform", "J4721", 3, 9, 63.5f, 29.6f, 2.5f), // padding verified

    ZWECKFORM_J8563 ("Zweckform", "J8563", 2, 7, 99.1f, 38.1f, 0f ),

    CUSTOM ("Format personnalis\u00E9", "", 3, 7, 70f, 41f, 0f);



    private String brand, model;
    private int columns, rows;
    private float width, height;
    private float horizontalSep;



    private StickerType(String brand, String model,
                        int columns, int rows,
                        float width, float height,
                        float horizontalSep)
    {
        this.brand = brand;
        this.model = model;
        this.columns = columns;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.horizontalSep = horizontalSep;
    }

    public String getBrand() {
        return brand;
    }

    public int getColumns() {
        return columns;
    }

    public float getHeight() {
        return height;
    }

    public String getModel() {
        return model;
    }

    public int getRows() {
        return rows;
    }

    public float getWidth() {
        return width;
    }

    public float getHorizontalSep() {
        return horizontalSep;
    }


}
