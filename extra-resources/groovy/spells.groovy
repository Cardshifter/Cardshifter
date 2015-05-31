CardDelegate.metaClass.spell << {Closure closure ->
    spell('Use', closure)
}
