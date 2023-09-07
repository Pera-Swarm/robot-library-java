package swarm.robot.types;

import swarm.robot.exception.ProximityException;

public class ProximityReadingType {
    private int[] distances;
    private RGBColorType[] colors;

    public ProximityReadingType(int[] angles, String str) throws ProximityException {
        int readingCount = angles.length;
        String[] values = str.split(" ");

        distances = new int[readingCount];
        colors = new RGBColorType[readingCount];

        if (values.length != readingCount * 2) {
            throw new ProximityException("ProximityReadingType: length mismatch " + values.length);
        }

        for (int i = 0; i < readingCount; i++) {
            // Reading distances
            if (values[i].compareTo("Infinity") == 0) {
                // -1 will be returned as a fail-proof option. Should throw an exception
                System.out.println("Proximity: Infinity reading received for " + i);
                distances[i] = -1;
            } else {
                distances[i] = Integer.parseInt(values[i]);
            }

            // Reading colors
            colors[i] = new RGBColorType(0, 0, 0);
            colors[i].setColorFromHexCode(values[readingCount + i]);
        }
    }

    public int[] distances() {
        return this.distances;
    }

    public RGBColorType[] colors() {
        return this.colors;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();

        // Distance values
        for (int i = 0; i < distances.length; i++)
            s.append(distances[i] + " ");

        s.append(" ");

        // Color values
        for (int i = 0; i < colors.length; i++)
            s.append(colors[i].toString() + " ");

        return s.toString();
    }
}
