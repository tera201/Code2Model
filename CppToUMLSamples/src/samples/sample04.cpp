namespace abstractClass {

class Figure
{
public:
    virtual double getSquare() = 0;
    virtual double getPerimeter() = 0;
    virtual void showFigureType() = 0;
};
}