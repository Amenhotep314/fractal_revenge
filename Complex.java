// A custom class to deal with complex numbers.

public class Complex {
    private double real;
    private double imaginary;

    public Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public double real() {
        return this.real;
    }
    public double im() {
        return this.imaginary;
    }

    public double modulus() {
        return Math.sqrt(real * real + imaginary * imaginary);
    }

    public double argument() {
        return Math.atan2(imaginary, real);
    }

    public Complex add(Complex other) {
        return new Complex(this.real + other.real, this.imaginary + other.imaginary);
    }

    public Complex pow(double exponent) {
        double r = Math.pow(this.modulus(), exponent);
        double theta = this.argument() * exponent;
        return new Complex(r * Math.cos(theta), r * Math.sin(theta));
    }
}
