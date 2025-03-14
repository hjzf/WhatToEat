pluginManagement {
    repositories {
//        maven("https://maven.aliyun.com/repository/public")
//        maven("https://maven.aliyun.com/repository/central")
//        maven("https://maven.aliyun.com/repository/google")
//        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven("https://maven.aliyun.com/repository/public")
//        maven("https://maven.aliyun.com/repository/central")
//        maven("https://maven.aliyun.com/repository/google")
//        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        mavenCentral()
    }
}

rootProject.name = "WhatToEat"
include(":app")
 