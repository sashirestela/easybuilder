@import java.util.List
@import io.github.sashirestela.easybuilder.model.RecordComponent
@import gg.jte.support.ForSupport

@param String packageName
@param String recordName
@param String builderName
@param List<RecordComponent> recordComponents
@if(!packageName.isEmpty())package ${packageName};
@endif
@javax.annotation.processing.Generated("io.github.sashirestela.easybuilder.processor.BuilderProcessor")
public class ${builderName} {

    // Fields
    @for(var recComp : recordComponents)private ${recComp.getType()} ${recComp.getName()};
    @endfor

    // Constructor
    private ${builderName}() {
    }

    // Static method to create a new builder
    public static ${builderName} builder() {
        return new ${builderName}();
    }

    // Static method to create a new builder from a record
    public static ${builderName} builder(${recordName} other) {
        ${builderName} one = new ${builderName}();
        @for(var recComp : recordComponents)one.${recComp.getName()} = other.${recComp.getName()}();
        @endfor
        return one;
    }

    // Accesors
    @for(var recComp : recordComponents)
    public ${builderName} ${recComp.getName()}(${recComp.getType()} ${recComp.getName()}) {
        this.${recComp.getName()} = ${recComp.getName()};
        return this;
    }
    @endfor

    // Withers
    @for(var extRecComp : recordComponents)
    public ${recordName} ${extRecComp.getNameForWith()}(${extRecComp.getType()} ${extRecComp.getName()}) {
        return new ${recordName}(
            @for(var recComp : ForSupport.of(recordComponents))@if(!recComp.get().getName().equals(extRecComp.getName()))this.@endif${recComp.get().getName()}@if(!recComp.isLast()),@endif
            @endfor);
    }
    @endfor

    // Build method
    public ${recordName} build() {
        return new ${recordName}(
            @for(var recComp : ForSupport.of(recordComponents))this.${recComp.get().getName()}@if(!recComp.isLast()),@endif
            @endfor);
    }
}
