/* Define Spell cards as using the Use closure.
 * See a mod's Game.groovy for detail on the Use action.
 * @author Simon Forsberg
 */

cardExtension('spell') {Closure closure ->
    spell('Use', closure)
}
