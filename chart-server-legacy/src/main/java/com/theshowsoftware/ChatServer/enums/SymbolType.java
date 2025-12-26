package com.theshowsoftware.ChatServer.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SymbolType {

    USDT("USDT", "0x0000"),
    A("A", "0x0001"),
    AAVE("AAVE", "0x0002"),
    ADA("ADA", "0x0003"),
    ANIME("ANIME", "0x0004"),
    APE("APE", "0x0005"),
    APT("APT", "0x0006"),
    ARB("ARB", "0x0007"),
    AVAX("AVAX", "0x0008"),
    BNB("BNB", "0x0009"),
    BTC("BTC", "0x000A"),
    CAKE("CAKE", "0x000B"),
    COMP("COMP", "0x000C"),
    CRV("CRV", "0x000D"),
    DEGO("DEGO", "0x000E"),
    DEXE("DEXE", "0x000F"),
    DOGE("DOGE", "0x0010"),
    DOT("DOT", "0x0011"),
    DYDX("DYDX", "0x0012"),
    EIGEN("EIGEN", "0x0013"),
    ENA("ENA", "0x0014"),
    ETH("ETH", "0x0015"),
    ETHFI("ETHFI", "0x0016"),
    FDUSD("FDUSD", "0x0017"),
    FLOKI("FLOKI", "0x0018"),
    HBAR("HBAR", "0x0019"),
    HUMA("HUMA", "0x001A"),
    ICP("ICP", "0x001B"),
    INIT("INIT", "0x001C"),
    LINK("LINK", "0x001D"),
    LPT("LPT", "0x001E"),
    LTC("LTC", "0x001F"),
    MASK("MASK", "0x0020"),
    MKR("MKR", "0x0021"),
    NEAR("NEAR", "0x0022"),
    NEIRO("NEIRO", "0x0023"),
    NXPC("NXPC", "0x0024"),
    ONDO("ONDO", "0x0025"),
    ORDI("ORDI", "0x0026"),
    PAXG("PAXG", "0x0027"),
    PENDLE("PENDLE", "0x0028"),
    PENGU("PENGU", "0x0029"),
    PEPE("PEPE", "0x002A"),
    PNUT("PNUT", "0x002B"),
    RENDER("RENDER", "0x002C"),
    S("S", "0x002D"),
    SOL("SOL", "0x002E"),
    SOPH("SOPH", "0x002F"),
    SUI("SUI", "0x0030"),
    SYRUP("SYRUP", "0x0031"),
    TAO("TAO", "0x0032"),
    TON("TON", "0x0033"),
    TRB("TRB", "0x0034"),
    TRUMP("TRUMP", "0x0035"),
    TRX("TRX", "0x0036"),
    UNI("UNI", "0x0037"),
    USDC("USDC", "0x0038"),
    VANA("VANA", "0x0039"),
    VIRTUAL("VIRTUAL", "0x003A"),
    WBTC("WBTC", "0x003B"),
    WCT("WCT", "0x003C"),
    WIF("WIF", "0x003D"),
    WLD("WLD", "0x003E"),
    ;

    private final String symbol;
    private final String hexCode;

    @Override
    public String toString() {
        return this.symbol;
    }

    public String getHexCodeString() {
        return hexCode;
    }

    public static SymbolType fromHexCode(String hexCode) {
        for (SymbolType s : values()) {
            if (s.getHexCodeString().equalsIgnoreCase(hexCode)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown hexCode: " + hexCode);
    }

    public static SymbolType fromSymbol(String symbol) {
        for (SymbolType s : values()) {
            if (s.symbol.equalsIgnoreCase(symbol)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown symbol: " + symbol);
    }
}