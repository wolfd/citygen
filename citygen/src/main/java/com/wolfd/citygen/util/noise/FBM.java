package com.wolfd.citygen.util.noise;

import java.util.Random;

public class FBM {
    private double delta = 16; //scales down all layers, probably would be better in the constructor
    public int octaves; //number of raw Perlin noise samples
    private double[] offset; //picks the z-value for each layer
    private double[] cos; //cosine table for rotating the noise layers
    private double[] sin;
    private double[] amplitude; //how prominent each layer is
    private double[] frequency; //scale of each layer
    private OpenSimplexNoise noise;

    public FBM(int octaves, long seed){
        this(octaves, seed, .5);
    }

    public FBM(int octaves, long seed, double persistence){ //.5 is a good value for persistence. Watch out for very high octaves too.
        noise = new OpenSimplexNoise(seed);
        this.octaves = octaves;

        Random r = new Random(seed);
        offset = new double[octaves];
        cos = new double[octaves];
        sin = new double[octaves];
        amplitude = new double[octaves];
        this.frequency = new double[octaves];
        for(int oc=0; oc<octaves; oc++){ //for every octave/layer
            double angle = r.nextDouble()*360; //I like degrees. Okay?
            cos[oc] = Math.cos(Math.toRadians(angle));
            sin[oc] = Math.sin(Math.toRadians(angle));

            offset[oc] = r.nextDouble()*256;

            frequency[oc] = Math.pow(2, oc);
            amplitude[oc] = Math.pow(persistence, oc);
        }

    }

    public double noise(double x, double y){ //I only care about 2d noise, but it shouldn't be too hard to make this 3d
        double total = 0;
        for(int i=0; i<this.octaves; i++){ //for all layers
            double xV = x*cos[i] + y*-sin[i]; //rotated value of x
            double yV = x*sin[i] + y*cos[i]; //rotated value of y
            double val = (noise.eval(xV * frequency[i] / delta, yV * frequency[i] / delta, offset[i]));
            val = (val*amplitude[i]); //amplitude decreases with every octave
            total += val;
        }
        return (total+1)/2; //tries to get it from 0 to 1
    }

}