package swarm.robot.types;

import swarm.robot.exception.RGBColorException;

public class RGBColorType {
    private int R, G, B;

    public RGBColorType(int R, int G, int B) {
        if (R < 0 || R > 255 || G < 0 || G > 255 || B < 0 || B > 255) {
            throw new IllegalArgumentException("R,G and B values must be between 0 and 255");
        } else {
            this.R = R;
            this.G = G;
            this.B = B;
        }
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
        setColor(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
    }

    public void setColor(int R, int G, int B) {
        if (R < 0 || R > 255 || G < 0 || G > 255 || B < 0 || B > 255) {
            try {
                throw new RGBColorException(R, G, B);
            } catch (RGBColorException e) {
                e.printStackTrace();
            }
        } else {
            this.R = R;
            this.G = G;
            this.B = B;
        }
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
