###Cardshifter modding documentation

---

#Card Library Guide

Creating and modifying the available cards is the quickest and easiest way to customize your game modification _[mod]_. This guide will explain in a detailed manner what are the different attributes, properties and effects, and how to use them effectively. 

The card library uses Plain Old JavaScript Object Notation _[POJSON]_ to store card information. This is not to be confused with the more typical JSON. JSON uses quotation marks for all field identifiers and values. POJSON only uses quotation make for String data, e.g., `"Hello, Cardshifter!"`. All identifiers, as well as numeric and boolean (`true/false`) values should be written with no quotation marks.

---

##General attributes

###`name`

- **Required**
- The unique identifier of a specific card. You must not have multiple cards with the same name, otherwise the game behavior could be unpredictable. 

Usage:

    {
        name: "My new card",
    },
    
###`flavor`

- Optional
- Flavor text that says something interesting about the card. 
 
Usage:

    {
        name: "My new card",
        flavor: "This is my favorite card of all. -Phrancis",
    },
