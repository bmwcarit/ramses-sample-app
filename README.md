# Ramses sample app

This application demonstrates the usage of the [Ramses rendering engine](https://ramses-sdk.readthedocs.io/en/latest/index.html) alongside
a 3D vehicle model of a BMW X5 (2018). The application is written in Kotlin and
the vehicle resource is built using the [Ramses Composer](https://github.com/bmwcarit/ramses-composer) GUI
v0.13.1. You can find a modifiable project with the car [on Github](https://github.com/bmwcarit/digital-car-3d).

For building the app and experimenting with the code a github personal access token is necessary.
This is because the app uses a Ramses-Android AAR package that is uploaded to GithubPackages and this requires a personal access token.
For further information how to create a github authentication token take a look at the [githubs guide](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token).

Now you only need to use that token for this repo.
For this to work just uncomment line 18 and 19 in the [gradle.properties file](./gradle.properties) and substitute the two strings "USERNAME" and
"GH_PERSONAL_ACCESS_TOKEN" with your github username and the github personal access token.

If everything worked, you should see a 3D car as the first of two navigable fragments:

![Car](./doc/screenshot.png)

# License

The application source code is under the [MPL 2.0 license](./LICENSE.txt), the same license as the Ramses AAR,
Ramses itself and the Ramses Logic.

The digital car model is licensed under CC-BY-4.0 (see [the source repository](https://github.com/bmwcarit/digital-car-3d)).

The binary files of the digital car model that are located in the apps [asset folder](./app/src/main/assets) are also licensed under CC-BY-4.0 (see [the source repository](https://github.com/bmwcarit/digital-car-3d)).
