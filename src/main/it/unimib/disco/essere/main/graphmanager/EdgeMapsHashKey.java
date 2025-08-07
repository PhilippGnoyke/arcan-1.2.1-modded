package it.unimib.disco.essere.main.graphmanager;

import java.util.Objects;

public class EdgeMapsHashKey
{
    private final String part1;
    private final String part2;
    private final String part3;


    public EdgeMapsHashKey(String part1, String part2, String part3)
    {
        this.part1 = part1;
        this.part2 = part2;
        this.part3 = part3;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (!(o instanceof EdgeMapsHashKey)) { return false; }
        EdgeMapsHashKey key = (EdgeMapsHashKey) o;
        return Objects.equals(part1, key.part1) &&
            Objects.equals(part2, key.part2) &&
            Objects.equals(part3, key.part3);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(part1, part2, part3);
    }

}
