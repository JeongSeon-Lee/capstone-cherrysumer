pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://repository.map.naver.com/archive/maven")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenCentral() {
            maven("https://repository.map.naver.com/archive/maven") // 네이버 지도
            maven("https://jitpack.io") // Stomp 라이브러리 필요
        }
    }
}

rootProject.name = "CherrySumer"
include(":app")
