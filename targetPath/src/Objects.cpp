namespace Objects {

class Device {
}

class Computer: public Device {
}

class Monitor: public Device {
}

class Laptop: public Computer, protected Monitor {
}

} // Objects 
