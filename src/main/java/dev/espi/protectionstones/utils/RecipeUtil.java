/*
 * Copyright 2019 ProtectionStones team and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.espi.protectionstones.utils;

import dev.espi.protectionstones.PSProtectBlock;
import dev.espi.protectionstones.ProtectionStones;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RecipeUtil {

    private static List<NamespacedKey> recipes = new ArrayList<>();
    public static void setupPSRecipes() {
        for (PSProtectBlock b : ProtectionStones.getInstance().getConfiguredBlocks()) {
            // add custom recipes to Bukkit
            if (b.allowCraftWithCustomRecipe) {
                try {
                    Bukkit.addRecipe(parseRecipe(b));
                    recipes.add(getNamespacedKeyForBlock(b));
                } catch (IllegalStateException e) {
                    ProtectionStones.getPluginLogger().warning("Reloading custom recipes does not work right now, you have to restart the server for updated recipes.");
                }
            }
        }
    }
    public static void removePSRecipes() {
        // remove previous protectionstones recipes (/ps reload)
        Iterator<Recipe> iter = Bukkit.getServer().recipeIterator();
        while (iter.hasNext()) {
            try {
                Recipe r = iter.next();
                if (r instanceof ShapedRecipe && (((ShapedRecipe) r).getKey().getNamespace().equalsIgnoreCase(ProtectionStones.getInstance().getName()))) {
                    iter.remove();
                }
            } catch (Exception ignored) {
            }
        }
        recipes.clear();
    }

    public static List<NamespacedKey> getRecipeKeys() {
        return recipes;
    }

    public static NamespacedKey getNamespacedKeyForBlock(PSProtectBlock block) {
        return new NamespacedKey(ProtectionStones.getInstance(), block.type.replaceAll("[+/=:]", ""));
    }

    public static ShapedRecipe parseRecipe(PSProtectBlock block) {
        // create item
        ItemStack item = block.createItem();
        item.setAmount(block.recipeAmount);

        // create recipe
        // key must adhere to [a-z0-9/._-]
        ShapedRecipe recipe = new ShapedRecipe(getNamespacedKeyForBlock(block), item);

        // parse config
        HashMap<String, Character> items = new HashMap<>();
        List<String> recipeLine = new ArrayList<>();
        char id = 'a';
        for (int i = 0; i < block.customRecipe.size(); i++) {
            recipeLine.add("");
            for (String mat : block.customRecipe.get(i)) {
                if (mat.equals("")) {
                    recipeLine.set(i, recipeLine.get(i) + " ");
                } else {
                    if (items.get(mat) == null) {
                        items.put(mat, id++);
                    }
                    recipeLine.set(i, recipeLine.get(i) + items.get(mat));
                }
            }
        }

        // recipe
        recipe.shape(recipeLine.toArray(new String[0]));
        for (String mat : items.keySet()) {
            if (Material.matchMaterial(mat) != null) { // general material type

                recipe.setIngredient(items.get(mat), Material.matchMaterial(mat));

            } else if (mat.startsWith("PROTECTION_STONES:")) { // ProtectionStones block

                // format PROTECTION_STONES:alias
                String alias = mat.substring(mat.indexOf(":") + 1);
                PSProtectBlock use = ProtectionStones.getProtectBlockFromAlias(alias);
                if (use != null && use.createItem() != null) {
                    recipe.setIngredient(items.get(mat), new RecipeChoice.ExactChoice(use.createItem()));
                } else {
                    ProtectionStones.getPluginLogger().warning("Unable to resolve material " + mat + " for the crafting recipe for " + block.alias + ".");
                }

            } else {
                ProtectionStones.getPluginLogger().warning("Unable to find material " + mat + " for the crafting recipe for " + block.alias + ".");
            }
        }

        return recipe;
    }
}
