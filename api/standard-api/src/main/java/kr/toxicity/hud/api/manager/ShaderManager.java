package kr.toxicity.hud.api.manager;

import kr.toxicity.hud.api.BetterHudAPI;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

/**
 * Shader manager
 */
public interface ShaderManager {
    /**
     * Defines constant (#Define)
     * @param key name
     * @param value value
     */
    void addConstant(@NotNull String key, @NotNull String value);

    /**
     * Adds shader tag to some shader file.
     * Default: #GenerateOtherMainMethod with empty list in all file.
     * @param type type of shader
     * @param supplier tag function
     */
    void addTagSupplier(@NotNull ShaderType type, @NotNull ShaderManager.ShaderTagSupplier supplier);

    /**
     * Empty tag.
     */
    ShaderTag EMPTY_TAG = newTag()
            .add("GenerateOtherMainMethod", Collections.emptyList())
            .add("GenerateOtherDefinedMethod", Collections.emptyList());
    /**
     * Empty tag supplier.
     */
    ShaderTagSupplier EMPTY_SUPPLIER = () -> EMPTY_TAG;


    /**
     * A supplier of shader tag.
     */
    @FunctionalInterface
    interface ShaderTagSupplier extends Supplier<ShaderTag> {
        /**
         * Pluses supplier with others.
         * @param other other supplier
         * @return new supplier
         */
        default ShaderTagSupplier plus(@NotNull ShaderManager.ShaderTagSupplier other) {
            return () -> get().plus(other.get());
        }
    }

    /**
     * Creates new shader tag
     * @return new tag
     */
    static @NotNull ShaderTag newTag() {
        return new ShaderTag();
    }

    /**
     * Shader tag.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class ShaderTag {
        private final Map<String, List<String>> lines = new HashMap<>();

        /**
         * Adds to tag
         * newTag()
         *  .add("GenerateOtherMainMethod", Collections.emptyList())
         *  .add("OtherYouWant", someList);
         * @param tag tag name
         * @param line tag list
         * @return self
         */
        public @NotNull ShaderTag add(@NotNull String tag, @NotNull List<String> line) {
            Objects.requireNonNull(tag);
            Objects.requireNonNull(line);
            var get = lines.get(tag);
            if (get == null) lines.put(tag, line);
            else {
                var list = new ArrayList<String>(get.size() + line.size());
                list.addAll(get);
                list.addAll(line);
                lines.put(tag, list);
            }
            return this;
        }

        /**
         * Sums two different tags to new one.
         * @param tag other tag
         * @return new merged tag
         */
        public @NotNull ShaderTag plus(@NotNull ShaderTag tag) {
            Objects.requireNonNull(tag);
            var newTag = new ShaderTag();
            lines.forEach(newTag::add);
            tag.lines.forEach(newTag::add);
            return newTag;
        }

        /**
         * Gets a list from name
         * @param tagName name
         * @return tag list or null
         */
        @ApiStatus.Internal
        @Nullable
        public List<String> get(@NotNull String tagName) {
            return lines.get(tagName);
        }
    }

    /**
     * Represents BetterHud's shader files.
     */
    @RequiredArgsConstructor
    @Getter
    enum ShaderType {
        /**
         * text vsh
         */
        TEXT_VERTEX("text.vsh"),
        /**
         * text fsh
         */
        TEXT_FRAGMENT("text.fsh"),
        /**
         * item vsh
         */
        ITEM_VERTEX("item.vsh"),
        /**
         * item fsh
         */
        ITEM_FRAGMENT("item.fsh"),
        /**
         * block vsh
         */
        BLOCK_VERTEX("block.vsh"),
        /**
         * block fsh
         */
        BLOCK_FRAGMENT("block.fsh"),

        ;
        private final @NotNull String fileName;


        /**
         * Reads the all line of shader file.
         * @return all line.
         */
        public @NotNull List<String> lines() {
            var bootstrap = BetterHudAPI.inst().bootstrap();
            var dataFolder = bootstrap.dataFolder();
            var shaderLocation = new File(dataFolder, "shaders");
            if (!shaderLocation.exists() && !shaderLocation.mkdirs()) {
                bootstrap.logger().warn("Unable to create folder BetterHud/shaders.");
            }
            var dataFile = new File(shaderLocation, fileName);
            var lines = new ArrayList<String>();
            if (!dataFile.exists()) {
                try (
                        var resourceStream = Objects.requireNonNull(bootstrap.resource(fileName), "Unknown resource: " + fileName);
                        var fileStream = new FileOutputStream(dataFile);
                        var bufferedFileStream = new BufferedOutputStream(fileStream)
                ) {
                    var bytes = resourceStream.readAllBytes();
                    bufferedFileStream.write(bytes);
                    String line;
                    try (var read = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8))) {
                        while ((line = read.readLine()) != null) {
                            lines.add(line);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to read a lines of stream.");
                    }
                } catch (IOException e) {
                    throw new RuntimeException("plugin jar file has a problem.");
                }
            } else {
                String line;
                try (var read = new BufferedReader(new FileReader(dataFile, StandardCharsets.UTF_8))) {
                    while ((line = read.readLine()) != null) {
                        lines.add(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Unable to read a lines of stream.");
                }
            }
            return lines;
        }
    }
}
