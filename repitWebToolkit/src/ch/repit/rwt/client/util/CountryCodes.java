/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * TBD: are there international country names???
 * cf http://www.iso.org/iso/french_country_names_and_code_elements
 *
 * @author tc149752
 */
public class CountryCodes {

    public static final String SUISSE = "CH - SUISSE";
    public static final String CH = "CH";

    private static List<String> countries = new ArrayList<String>();

    private static SortedMap<String,String> countriesMap = new TreeMap();
    private static SortedMap<String,String> countryCodeMap = new TreeMap();

    
    public static List<String> listCountries() {
        return countries;
    }

    /**
     *
     * @return a map with iso-code as value and "ISO code - Country name" as key
     */
    public static SortedMap<String,String> getCountriesMap() {
        return countriesMap;
    }
    /**
     *
     * @return a map with iso-code as key and "ISO code - Country name" as value
     */
    public static SortedMap<String,String> getCountryCodeMap() {
        return countryCodeMap;
    }


    static {
        List<String> tmpc = new ArrayList<String>();
        tmpc.add(SUISSE);
        tmpc.add("AF - AFGHANISTAN");
        tmpc.add("ZA - AFRIQUE DU SUD");
        tmpc.add("AX - ÅLAND, ÎLES");
        tmpc.add("AL - ALBANIE");
        tmpc.add("DZ - ALGÉRIE");
        tmpc.add("DE - ALLEMAGNE");
        tmpc.add("AD - ANDORRE");
        tmpc.add("AO - ANGOLA");
        tmpc.add("AI - ANGUILLA");
        tmpc.add("AQ - ANTARCTIQUE");
        tmpc.add("AG - ANTIGUA ET BARBUDA");
        tmpc.add("AN - ANTILLES NÉERLANDAISES");
        tmpc.add("SA - ARABIE SAOUDITE");
        tmpc.add("AR - ARGENTINE");
        tmpc.add("AM - ARMÉNIE");
        tmpc.add("AW - ARUBA");
        tmpc.add("AU - AUSTRALIE");
        tmpc.add("AT - AUTRICHE");
        tmpc.add("AZ - AZERBAÏDJAN");
        tmpc.add("BS - BAHAMAS");
        tmpc.add("BH - BAHREÏN");
        tmpc.add("BD - BANGLADESH");
        tmpc.add("BB - BARBADE");
        tmpc.add("BY - BÉLARUS");
        tmpc.add("BE - BELGIQUE");
        tmpc.add("BZ - BELIZE");
        tmpc.add("BJ - BÉNIN");
        tmpc.add("BM - BERMUDES");
        tmpc.add("BT - BHOUTAN");
        tmpc.add("BO - BOLIVIE, l'ÉTAT PLURINATIONAL DE");
        tmpc.add("BA - BOSNIE-HERZÉGOVINE");
        tmpc.add("BW - BOTSWANA");
        tmpc.add("BV - BOUVET, ÎLE");
        tmpc.add("BR - BRÉSIL");
        tmpc.add("BN - BRUNÉI DARUSSALAM");
        tmpc.add("BG - BULGARIE");
        tmpc.add("BF - BURKINA FASO");
        tmpc.add("BI - BURUNDI");
        tmpc.add("KY - CAÏMANES, ÎLES");
        tmpc.add("KH - CAMBODGE");
        tmpc.add("CM - CAMEROUN");
        tmpc.add("CA - CANADA");
        tmpc.add("CV - CAP-VERT");
        tmpc.add("CF - CENTRAFRICAINE, RÉPUBLIQUE");
        tmpc.add("CL - CHILI");
        tmpc.add("CN - CHINE");
        tmpc.add("CX - CHRISTMAS, ÎLE");
        tmpc.add("CY - CHYPRE");
        tmpc.add("CC - COCOS (KEELING), ÎLES");
        tmpc.add("CO - COLOMBIE");
        tmpc.add("KM - COMORES");
        tmpc.add("CG - CONGO");
        tmpc.add("CD - CONGO, LA RÉPUBLIQUE DÉMOCRATIQUE DU");
        tmpc.add("CK - COOK, ÎLES");
        tmpc.add("KR - CORÉE, RÉPUBLIQUE DE");
        tmpc.add("KP - CORÉE, RÉPUBLIQUE POPULAIRE DÉMOCRATIQUE DE");
        tmpc.add("CR - COSTA RICA");
        tmpc.add("CI - CÔTE D'IVOIRE");
        tmpc.add("HR - CROATIE");
        tmpc.add("CU - CUBA");
        tmpc.add("DK - DANEMARK");
        tmpc.add("DJ - DJIBOUTI");
        tmpc.add("DO - DOMINICAINE, RÉPUBLIQUE");
        tmpc.add("DM - DOMINIQUE");
        tmpc.add("EG - ÉGYPTE");
        tmpc.add("SV - EL SALVADOR");
        tmpc.add("AE - ÉMIRATS ARABES UNIS");
        tmpc.add("EC - ÉQUATEUR");
        tmpc.add("ER - ÉRYTHRÉE");
        tmpc.add("ES - ESPAGNE");
        tmpc.add("EE - ESTONIE");
        tmpc.add("US - ÉTATS-UNIS");
        tmpc.add("ET - ÉTHIOPIE");
        tmpc.add("FK - FALKLAND, ÎLES (MALVINAS)");
        tmpc.add("FO - FÉROÉ, ÎLES");
        tmpc.add("FJ - FIDJI");
        tmpc.add("FI - FINLANDE");
        tmpc.add("FR - FRANCE");
        tmpc.add("GA - GABON");
        tmpc.add("GM - GAMBIE");
        tmpc.add("GE - GÉORGIE");
        tmpc.add("GS - GÉORGIE DU SUD ET LES ÎLES SANDWICH DU SUD");
        tmpc.add("GH - GHANA");
        tmpc.add("GI - GIBRALTAR");
        tmpc.add("GR - GRÈCE");
        tmpc.add("GD - GRENADE");
        tmpc.add("GL - GROENLAND");
        tmpc.add("GP - GUADELOUPE");
        tmpc.add("GU - GUAM");
        tmpc.add("GT - GUATEMALA");
        tmpc.add("GG - GUERNESEY");
        tmpc.add("GN - GUINÉE");
        tmpc.add("GQ - GUINÉE ÉQUATORIALE");
        tmpc.add("GW - GUINÉE-BISSAU");
        tmpc.add("GY - GUYANA");
        tmpc.add("GF - GUYANE FRANÇAISE");
        tmpc.add("HT - HAÏTI");
        tmpc.add("HM - HEARD, ÎLE ET MCDONALD, ÎLES");
        tmpc.add("HN - HONDURAS");
        tmpc.add("HK - HONG-KONG");
        tmpc.add("HU - HONGRIE");
        tmpc.add("IM - ÎLE DE MAN");
        tmpc.add("UM - ÎLES MINEURES ÉLOIGNÉES DES ÉTATS-UNIS");
        tmpc.add("VG - ÎLES VIERGES BRITANNIQUES");
        tmpc.add("VI - ÎLES VIERGES DES ÉTATS-UNIS");
        tmpc.add("IN - INDE");
        tmpc.add("ID - INDONÉSIE");
        tmpc.add("IR - IRAN, RÉPUBLIQUE ISLAMIQUE D'");
        tmpc.add("IQ - IRAQ");
        tmpc.add("IE - IRLANDE");
        tmpc.add("IS - ISLANDE");
        tmpc.add("IL - ISRAËL");
        tmpc.add("IT - ITALIE");
        tmpc.add("JM - JAMAÏQUE");
        tmpc.add("JP - JAPON");
        tmpc.add("JE - JERSEY");
        tmpc.add("JO - JORDANIE");
        tmpc.add("KZ - KAZAKHSTAN");
        tmpc.add("KE - KENYA");
        tmpc.add("KG - KIRGHIZISTAN");
        tmpc.add("KI - KIRIBATI");
        tmpc.add("KW - KOWEÏT");
        tmpc.add("LA - LAO, RÉPUBLIQUE DÉMOCRATIQUE POPULAIRE");
        tmpc.add("LS - LESOTHO");
        tmpc.add("LV - LETTONIE");
        tmpc.add("LB - LIBAN");
        tmpc.add("LR - LIBÉRIA");
        tmpc.add("LY - LIBYENNE, JAMAHIRIYA ARABE");
        tmpc.add("LI - LIECHTENSTEIN");
        tmpc.add("LT - LITUANIE");
        tmpc.add("LU - LUXEMBOURG");
        tmpc.add("MO - MACAO");
        tmpc.add("MK - MACÉDOINE, L'EX-RÉPUBLIQUE YOUGOSLAVE DE");
        tmpc.add("MG - MADAGASCAR");
        tmpc.add("MY - MALAISIE");
        tmpc.add("MW - MALAWI");
        tmpc.add("MV - MALDIVES");
        tmpc.add("ML - MALI");
        tmpc.add("MT - MALTE");
        tmpc.add("MP - MARIANNES DU NORD, ÎLES");
        tmpc.add("MA - MAROC");
        tmpc.add("MH - MARSHALL, ÎLES");
        tmpc.add("MQ - MARTINIQUE");
        tmpc.add("MU - MAURICE");
        tmpc.add("MR - MAURITANIE");
        tmpc.add("YT - MAYOTTE");
        tmpc.add("MX - MEXIQUE");
        tmpc.add("FM - MICRONÉSIE, ÉTATS FÉDÉRÉS DE");
        tmpc.add("MD - MOLDOVA");
        tmpc.add("MC - MONACO");
        tmpc.add("MN - MONGOLIE");
        tmpc.add("ME - MONTÉNÉGRO");
        tmpc.add("MS - MONTSERRAT");
        tmpc.add("MZ - MOZAMBIQUE");
        tmpc.add("MM - MYANMAR");
        tmpc.add("NA - NAMIBIE");
        tmpc.add("NR - NAURU");
        tmpc.add("NP - NÉPAL");
        tmpc.add("NI - NICARAGUA");
        tmpc.add("NE - NIGER");
        tmpc.add("NG - NIGÉRIA");
        tmpc.add("NU - NIUÉ");
        tmpc.add("NF - NORFOLK, ÎLE");
        tmpc.add("NO - NORVÈGE");
        tmpc.add("NC - NOUVELLE-CALÉDONIE");
        tmpc.add("NZ - NOUVELLE-ZÉLANDE");
        tmpc.add("IO - OCÉAN INDIEN, TERRITOIRE BRITANNIQUE DE L'");
        tmpc.add("OM - OMAN");
        tmpc.add("UG - OUGANDA");
        tmpc.add("UZ - OUZBÉKISTAN");
        tmpc.add("PK - PAKISTAN");
        tmpc.add("PW - PALAOS");
        tmpc.add("PS - PALESTINIEN OCCUPÉ, TERRITOIRE");
        tmpc.add("PA - PANAMA");
        tmpc.add("PG - PAPOUASIE-NOUVELLE-GUINÉE");
        tmpc.add("PY - PARAGUAY");
        tmpc.add("NL - PAYS-BAS");
        tmpc.add("PE - PÉROU");
        tmpc.add("PH - PHILIPPINES");
        tmpc.add("PN - PITCAIRN");
        tmpc.add("PL - POLOGNE");
        tmpc.add("PF - POLYNÉSIE FRANÇAISE");
        tmpc.add("PR - PORTO RICO");
        tmpc.add("PT - PORTUGAL");
        tmpc.add("QA - QATAR");
        tmpc.add("RE - RÉUNION");
        tmpc.add("RO - ROUMANIE");
        tmpc.add("GB - ROYAUME-UNI");
        tmpc.add("RU - RUSSIE, FÉDÉRATION DE");
        tmpc.add("RW - RWANDA");
        tmpc.add("EH - SAHARA OCCIDENTAL");
        tmpc.add("BL - SAINT-BARTHÉLEMY");
        tmpc.add("KN - SAINT-KITTS-ET-NEVIS");
        tmpc.add("SM - SAINT-MARIN");
        tmpc.add("MF - SAINT-MARTIN");
        tmpc.add("PM - SAINT-PIERRE-ET-MIQUELON");
        tmpc.add("VA - SAINT-SIÈGE (ÉTAT DE LA CITÉ DU VATICAN)");
        tmpc.add("VC - SAINT-VINCENT-ET-LES GRENADINES");
        tmpc.add("SH - SAINTE-HÉLÈNE");
        tmpc.add("LC - SAINTE-LUCIE");
        tmpc.add("SB - SALOMON, ÎLES");
        tmpc.add("WS - SAMOA");
        tmpc.add("AS - SAMOA AMÉRICAINES");
        tmpc.add("ST - SAO TOMÉ-ET-PRINCIPE");
        tmpc.add("SN - SÉNÉGAL");
        tmpc.add("RS - SERBIE");
        tmpc.add("SC - SEYCHELLES");
        tmpc.add("SL - SIERRA LEONE");
        tmpc.add("SG - SINGAPOUR");
        tmpc.add("SK - SLOVAQUIE");
        tmpc.add("SI - SLOVÉNIE");
        tmpc.add("SO - SOMALIE");
        tmpc.add("SD - SOUDAN");
        tmpc.add("LK - SRI LANKA");
        tmpc.add("SE - SUÈDE");
        tmpc.add("SR - SURINAME");
        tmpc.add("SJ - SVALBARD ET ÎLE JAN MAYEN");
        tmpc.add("SZ - SWAZILAND");
        tmpc.add("SY - SYRIENNE, RÉPUBLIQUE ARABE");
        tmpc.add("TJ - TADJIKISTAN");
        tmpc.add("TW - TAÏWAN, PROVINCE DE CHINE");
        tmpc.add("TZ - TANZANIE, RÉPUBLIQUE-UNIE DE");
        tmpc.add("TD - TCHAD");
        tmpc.add("CZ - TCHÈQUE, RÉPUBLIQUE");
        tmpc.add("TF - TERRES AUSTRALES FRANÇAISES");
        tmpc.add("TH - THAÏLANDE");
        tmpc.add("TL - TIMOR-LESTE");
        tmpc.add("TG - TOGO");
        tmpc.add("TK - TOKELAU");
        tmpc.add("TO - TONGA");
        tmpc.add("TT - TRINITÉ-ET-TOBAGO");
        tmpc.add("TN - TUNISIE");
        tmpc.add("TM - TURKMÉNISTAN");
        tmpc.add("TC - TURKS ET CAÏQUES, ÎLES");
        tmpc.add("TR - TURQUIE");
        tmpc.add("TV - TUVALU");
        tmpc.add("UA - UKRAINE");
        tmpc.add("UY - URUGUAY");
        tmpc.add("VU - VANUATU");
        tmpc.add("VE - VENEZUELA, RÉPUBLIQUE BOLIVARIENNE DU");
        tmpc.add("VN - VIET NAM");
        tmpc.add("WF - WALLIS ET FUTUNA");
        tmpc.add("YE - YÉMEN");
        tmpc.add("ZM - ZAMBIE");
        tmpc.add("ZW - ZIMBABWE");
        countries = Collections.unmodifiableList(tmpc);

        TreeMap<String,String> tmpm1 = new TreeMap();
        TreeMap<String,String> tmpm2 = new TreeMap();
        for (String c : tmpc) {
            String[] cs = c.split("-");
            tmpm1.put(c, cs[0].trim()); //cs[1].trim());
            tmpm2.put(cs[0].trim(), c); //cs[1].trim());
        }
        countriesMap = Collections.unmodifiableSortedMap(tmpm1);
        countryCodeMap = Collections.unmodifiableSortedMap(tmpm2);
    }


}
