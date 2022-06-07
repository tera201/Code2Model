namespace abstractClass {

class Figure
{
public:
    virtual double getSquare() = 0;
    virtual double getPerimeter() {  return 0.000001;  }
    virtual void showFigureType() = 0;
};
}