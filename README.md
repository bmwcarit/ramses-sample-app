# Ramses sample app

![Car](./doc/screenshot.png)

This application demonstrates the usage of the [Ramses rendering engine](https://ramses-sdk.readthedocs.io/en/latest/index.html) alongside
a 3D vehicle model of a BMW X5 (2018). The application is written in Kotlin and
the vehicle resource is built using the [Ramses Composer](https://github.com/bmwcarit/ramses-composer) GUI
v1.0.0. You can find a modifiable project with the car [on Github](https://github.com/bmwcarit/digital-car-3d).


The app has two UI fragments. The first fragment shows a surface with 3D view of a BMW X5 model. The second fragment shows a static
logo image. It is possible to mix Ramses 3D content with other Android UI elements in various
ways. Be creative!

Please refer to the [Ramses Website](https://ramses3d.org) for further materials and information on the Ramses Project.

# License

The application source code is under the [MPL 2.0 license](./LICENSE.txt), the same license as the Ramses AAR,
Ramses itself and the Ramses Logic.

The digital car model is licensed under CC-BY-4.0 (see [the source repository](https://github.com/bmwcarit/digital-car-3d)).

The binary files of the digital car model that are located in the apps [asset folder](./app/src/main/assets) are
also licensed under CC-BY-4.0 (see [the source repository](https://github.com/bmwcarit/digital-car-3d)).
