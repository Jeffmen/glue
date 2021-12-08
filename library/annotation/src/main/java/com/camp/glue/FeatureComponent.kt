package com.camp.glue

interface FeatureComponent<T : IComponent> {
    val api: T
}