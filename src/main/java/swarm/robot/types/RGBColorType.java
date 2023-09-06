package swarm.robot.types;

import swarm.robot.exception.RGBColorException;

public class RGBColorType {
    private int R, G, B;

    /**
     * RGBColorType class
     * 
     * @param R Red intensity, [0,255]
     * @param G Green intensity, [0,255]
     * @param B Blue intensity, [0,255]
     */
    public RGBColorType(int R, int G, int B) {
        if (R < 0 || R > 255 || G < 0 || G > 255 || B < 0 || B > 255) {
            throw new IllegalArgumentException("R,G,B values must be between 0 and 255");
        } else {
            this.R = R;
            this.G = G;
            this.B = B;
        }
    }

    /**
     * RGBColorType class
     * 
     * @param color int[3], where colors are in order {R,G,B}
     */
    public RGBColorType(int[] color) {
        if (color.length != 3) {
            throw new IllegalArgumentException("length of the color[] should be equal to 3");
        } else if (color[0] < 0 || color[0] > 255 || color[1] < 0 || color[1] > 255 || color[2] < 0 || color[2] > 255) {
            throw new IllegalArgumentException("R,G and B values must be between 0 and 255");
        } else {
            this.R = color[0];
            this.G = color[1];
            this.B = color[2];
        }
    }

    /**
     * RGBColorType class
     * 
     * @param str string, where colors are in order "R G B"
     */
    public RGBColorType(String str) {
        this.setColor(str);
    }

    public void setColor(String str) {
        String[] color = str.split(" ");

        if (color.length != 4) {
            try {
                throw new RGBColorException("length != 4");
            } catch (RGBColorException e) {
                e.printStackTrace();
            }
        }
        setColor(validate(color[0]), validate(color[1]), validate(color[2]));
    }

    public void setColor(int R, int G, int B) {
        this.R = validate(R);
        this.G = validate(G);
        this.B = validate(B);
    }

    private int validate(String s) {
        return validate(Integer.parseInt(s));
    }

    private int validate(int i) {
        if (i < 0 || i > 255) {
            try {
                throw new RGBColorException(R, G, B);
            } catch (RGBColorException e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    public int getR() {
        return this.R;
    }

    public int getB() {
        return this.B;
    }

    public int getG() {
        return this.G;
    }

    public int[] getColor() {
        return new int[] { this.R, this.G, this.B };
    }

    public String toString() {
        return "R:" + this.R + ", G:" + this.G + ", B:" + this.B;
    }

    public String toStringColor() {
        return this.R + " " + this.G + " " + this.B;
    }

    public boolean compareTo(RGBColorType color) {
        return (color.getR() == this.R) && (color.getG() == this.G) && (color.getB() == this.B);
    }
}
