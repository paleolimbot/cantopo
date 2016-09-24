package fwe.nts;

import java.util.ArrayList;
import java.util.List;

import fwe.locations.geometry.Bounds;
import fwe.locations.geometry.LatLon;

public class NTSMapSheet {

	public static final int SCALE_SERIES = 0 ;
	public static final int SCALE_250K = 1 ;
	public static final int SCALE_50K = 2 ;
	public static final int SCALE_BLOCK = 3 ;

	private static final Bounds BOUNDS = new Bounds(-144, -48, 40, 88);
	private Bounds bounds ;
	private String ntsId ;
	private int[] tile50 ;
	private int[] tile250 ;
	private int[] tileSeries ;
	private int[] tileBlock ;
	private String name ;
	private int scale ;

	private NTSMapSheet() {
		//no creating this by the public
	}

	public Bounds getBounds() {
		return bounds;
	}

	public String getNtsId() {
		return ntsId;
	}

	public int[] getTileBlock() {
		return tileBlock ;
	}

	public int[] getTile50() {
		return tile50;
	}

	public int[] getTile250() {
		return tile250 ;
	}

	public int[] getTileSeries() {
		return tileSeries ;
	}

	public String getName() {
		return name;
	}

	public int getScale() {
		return scale ;
	}

	public int possibleSubSheets() {
		switch(this.getScale()) {
		case NTSMapSheet.SCALE_SERIES:
			if(tileSeries[1] >= 7)
				return 8 ;
			else
				return 16 ;
		case NTSMapSheet.SCALE_250K:
			return 16 ;
		case NTSMapSheet.SCALE_50K:
			return 12 ;
		default:
			return 0 ;
		}
	}

	public boolean equals(Object other) {
		return this.toString().equals(other.toString()) ;
	}

	public String toString() {
		return this.getNtsId() ;
	}

	private static NTSMapSheet mapSheetFromTile(int scale, int[] tile) {
		if(validTile(scale, tile)) {
			NTSMapSheet out = new NTSMapSheet() ;
			out.ntsId = ntsStringId(scale, tile) ;
			out.scale = scale ;
			switch(scale) {
			case SCALE_SERIES:
				out.tileSeries = tile ;
				out.bounds = boundsSeries(tile) ;
				break ;
			case SCALE_250K:
				out.tile250 = tile ;
				out.tileSeries = tileSeriesFromTile250(tile) ;
				out.bounds = bounds250(tile) ;
				break ;
			case SCALE_50K:
				out.tile50 = tile ;
				out.tile250 = tile250FromTile50(tile) ;
				out.tileSeries = tileSeriesFromTile250(out.tile250) ;
				out.bounds = bounds50(tile) ;
				break ;
			case SCALE_BLOCK:
				out.tileBlock = tile ;
				out.tile50 = tile50FromTileBlock(tile) ;
				out.tile250 = tile250FromTile50(out.tile50) ;
				out.tileSeries = tileSeriesFromTile250(out.tile250) ;
				out.bounds = boundsBlock(tile) ;
				break ;
			default:
				throw new IllegalArgumentException("Invaid scale") ;
			}
			out.name = null ;
			return out ;
		} else {
			return null ;
		}
	}

	public static NTSMapSheet getSheetById(String ntsId) {
		int[] tile = tileById(ntsId) ;
		if(tile != null) {
			int scale = scaleById(ntsId) ;
			return mapSheetFromTile(scale, tile) ;
		} else {
			return null ;
		}
	}

	public static NTSMapSheet getSheetByLatLon(int scale, LatLon point) {
		int[] tile = tileByLatLon(scale, point) ;
		if(tile != null) {
			return mapSheetFromTile(scale, tile) ;
		} else {
			return null ;
		}
	}

	public static List<NTSMapSheet> getSheetsByBounds(int scale, Bounds bounds) {
		List<int[]> tiles = tilesByBounds(scale, bounds) ;
		List<NTSMapSheet> sheets = new ArrayList<NTSMapSheet>() ;
		for(int[] tile: tiles) {
			NTSMapSheet sheet = mapSheetFromTile(scale, tile) ;
			if(sheet != null)
				sheets.add(sheet) ;
		}
		return sheets ;
	}

	public static List<NTSMapSheet> getChildSheets(NTSMapSheet sheet) {
		int scale = -1 ;
		List<int[]> tiles = null ;
		switch(sheet.getScale()) {
		case SCALE_SERIES:
			tiles = tiles250ByTileSeries(sheet.tileSeries) ;
			scale = SCALE_250K ;
			break ;
		case SCALE_250K:
			tiles = tiles50ByTile250(sheet.tile250) ;
			scale = SCALE_50K ;
			break ;
		case SCALE_50K:
			tiles = tilesBlockByTile50(sheet.tile50) ;
			scale = SCALE_BLOCK ;
			break ;
		default:
			throw new IllegalArgumentException("Sheet at given scale has no child sheets") ;
		}

		List<NTSMapSheet> out = new ArrayList<NTSMapSheet>() ;
		for(int[] tile: tiles) {
			NTSMapSheet result = mapSheetFromTile(scale, tile) ;
			if(result != null)
				out.add(result) ;
		}
		return out ;
	}

	//	__MAP_SERIES_N_OF_80 = (("910", "780", "560", "340", "120"), #Series row 10 and 11
	//            (None, "781", "561", "341", "121"))

	public static final String[][] MAP_SERIES_N_OF_80 = {{"910", "780", "560", "340", "120"},
		{null, "781", "561", "341", "121"}} ;

	//__MAP_250K = (("D", "C", "B", "A"),
	//  ("E", "F", "G", "H"),
	//  ("L", "K", "J", "I"),
	//  ("M", "N", "O", "P"))

	public static final String[][] MAP_250K = {{"D", "C", "B", "A"},
		{"E", "F", "G", "H"},
		{"L", "K", "J", "I"},
		{"M", "N", "O", "P"}} ;

	//__MAP_250K_N_OF_68 = (("B", "A"),
	//          ("C", "D"),
	//          ("F", "E"),
	//          ("G", "H"))

	public static final String[][] MAP_250K_N_OF_68 = {{"B", "A"},
		{"C", "D"},
		{"F", "E"},
		{"G", "H"}} ;

	//__MAP_50K = ((4, 3, 2, 1),
	//  (5, 6, 7, 8),
	//  (12, 11, 10, 9),
	//  (13, 14, 15, 16))

	public static final String[][] MAP_50K = {{"4", "3", "2", "1"},
		{"5", "6", "7", "8"},
		{"12", "11", "10", "9"},
		{"13", "14", "15", "16"}} ;

	public static final String[][] MAP_BLOCK = {	{"D", "C", "B", "A"},
		{"E", "F", "G", "H"},
		{"L", "K", "J", "I"}} ;

	//
	//def __indexXY(value, valueMap):
	//for y in range(len(valueMap)):
	//xVals = valueMap[y]
	//for x in range(len(xVals)):
	//  if xVals[x] == value:
	//      return x, y
	//return None
	//

	private static int[] indexXY(String value, String[][] valueMap) {
		for(int y=0; y<valueMap.length; y++) {
			for(int x=0; x<valueMap[y].length; x++) {
				if(value.equals(valueMap[y][x]))
					return new int[] {x, y} ;
			}
		}
		return null ;
	}

	//def __widthAndOffset250(lat):
	//if lat >= 80.0:
	//#width is 8 degrees north of 80th parallel, grid starts 8 degrees east of 144 west
	//width = 8.0
	//offset = 8.0
	//elif lat >= 68.0:
	//width = 4.0
	//offset = 0
	//else:
	//width = 2.0
	//offset = 0
	//return width, offset

	private static double[] widthAndOffset250(double lat) {
		double width = 2.0 ;
		double offset = 0 ;
		if(lat >= 80.0) {
			width = 8.0 ;
			offset = 8.0 ;
		} else if(lat >= 68.0) {
			width = 4.0 ;
			offset = 0 ;
		}
		return new double[] {width, offset} ;
	}

	//def __widthAndOffsetSeries(lat):
	//if lat >= 80.0:
	//#width is 16 degrees north of 80th parallel, grid starts 8 degrees east of 144 west
	//width = 16.0
	//offset = 8.0
	//else:
	//width = 8.0
	//offset = 0
	//return width, offset
	//

	private static double[] widthAndOffsetSeries(double lat) {
		double width = 8 ;
		double offset = 0 ;
		if(lat >= 80.0) {
			width = 16 ;
			offset = 8 ;
		}
		return new double[] {width, offset} ;
	}

	//def __mapsPerSeries(tile250kY):
	//if tile250kY >= 28:
	//#2 maps per series north of 68
	//return 2
	//else:
	//return 4
	//

	private static int mapsPerSeries(int tile250kY) {
		if(tile250kY >= 28) {
			return 2 ;
		} else {
			return 4 ;
		}
	}

	//def __tileSeriesY(lat):
	//return int((lat - 40) / 4.0)
	//

	private static int tileSeriesY(double lat) {
		return (int)Math.round(Math.floor((lat-40.0) / 4.0)) ;
	}

	//def __tileSeriesX(lon, lat):
	//width, offset = __widthAndOffsetSeries(lat)
	//return int((lon + (144 - offset)) / width)
	//

	private static int tileSeriesX(double lon, double lat) {
		double[] widthAndOffset = widthAndOffsetSeries(lat) ;
		return (int)Math.round(Math.floor((lon+(144-widthAndOffset[1]))/widthAndOffset[0])) ;
	}

	//def __tileSeries(lon, lat):
	//return (__tileSeriesX(lon, lat), __tileSeriesY(lat))
	//

	private static int[] tileSeries(double lon, double lat) {
		return new int[] {tileSeriesX(lon, lat), tileSeriesY(lat)} ;
	}

	//def __boundsSeries(tile):
	//minlat = tile[1] * 4.0 + 40
	//width, offset = __widthAndOffsetSeries(minlat)
	//minlon = -144 + tile[0] * width + offset
	//return Bounds(minlon, minlon + width, minlat, minlat + 4.0)

	private static Bounds boundsSeries(int[] tile) {
		double minlat = tile[1] * 4.0 + 40.0 ;
		double[] wo = widthAndOffsetSeries(minlat) ;
		double minlon = -144 + tile[0] * wo[0] + wo[1] ;
		return new Bounds(minlon, minlon+wo[0], minlat, minlat+4) ;
	}

	//def __ntsIdSeries(tile):
	//if tile[1] >= 10 and tile[0] <= 4:
	//return __MAP_SERIES_N_OF_80[tile[1]-10][tile[0]]
	//else:
	//seriesRow = str(tile[1])
	//seriesX = 11 - tile[0]
	//seriesColumn = str(seriesX)
	//if len(seriesColumn) == 1:
	//  seriesColumn = "0" + seriesColumn
	//return seriesColumn + seriesRow
	//

	private static String ntsIdSeries(int[] tile) {
		if(tile[1] >= 10 && tile[0] <=4) {
			return MAP_SERIES_N_OF_80[tile[1]-10][tile[0]] ;
		} else {
			String seriesRow = String.valueOf(tile[1]) ;
			int seriesX = 11 - tile[0] ;
			String seriesColumn = String.valueOf(seriesX) ;
			if(seriesColumn.length() == 1) {
				seriesColumn = "0" + seriesColumn ;
			}
			return seriesColumn + seriesRow ;
		}
	}

	//def __tileSeriesById(series):
	//if len(series) >= 2:
	//result = __indexXY(series, __MAP_SERIES_N_OF_80)
	//if result is not None:
	//  seriesX = result[0]
	//  seriesY = result[1] + 10
	//else:
	//  seriesY = int(series[-1:])
	//  seriesX = 11 - int(series[:-1])
	//return (seriesX, seriesY)
	//else:
	//return None

	private static int[] tileSeriesById(String series) {
		if(series.length() >= 2) {
			int[] result = indexXY(series, MAP_SERIES_N_OF_80) ;
			if(result != null) {
				return new int[] {result[0], result[1]+10} ;
			} else {
				int seriesY = Integer.valueOf(series.substring(series.length()-1)) ;
				int seriesX = 11 - Integer.valueOf(series.substring(0, series.length()-1)) ;
				return new int[] {seriesX, seriesY} ;
			}
		} else {
			return null ;
		}
	}

	//def __validTileSeries(tile):
	//if tile[1] == 0:
	//if 7 <= tile[0] <= 11:
	//  return True
	//elif tile[1] == 1:
	//if 6 <= tile[0] <= 11 or tile[0] == 1 or tile[0] == 2:
	//  return True
	//elif 2 <= tile[1] <= 4:
	//if 0 <= tile[0] <= 11:
	//  return True
	//elif 5 <= tile[1] <= 8:
	//if 0 <= tile[0] <= 10:
	//  return True
	//elif tile[1] == 9:
	//if 0 <= tile[0] <= 9:
	//  return True
	//elif tile[1] == 10:
	//if 0 <= tile[0] <= 4:
	//  return True
	//elif tile[1] == 11:
	//if 1 <= tile[0] <= 4:
	//  return True
	//return False

	private static boolean validTileSeries(int[] tile) {
		if(tile[1] == 0) {
			if(7 <= tile[0] && tile[0] <= 11) {
				return true ;
			}
		} else if(tile[1] == 1) {
			if(6 <= tile[0] && tile[0] <= 11 || tile[0] == 1 || tile[0] == 2) {
				return true ;
			}
		} else if(2 <= tile[1] && tile[1] <= 4) {
			if(0 <= tile[0] && tile[0] <= 11) {
				return true ;
			}
		} else if(5 <= tile[1] && tile[1] <= 8) {
			if(0 <= tile[0] && tile[0] <= 10) {
				return true ;
			}
		} else if(tile[1] == 9) {
			if(0 <= tile[0] && tile[0] <= 9) {
				return true ;
			}
		} else if(tile[1] == 10) {
			if(0 <= tile[0] && tile[0] <= 4) {
				return true ;
			}
		} else if(tile[1] == 11) {
			if(1 <= tile[0] && tile[0] <= 4) {
				return true ;
			}
		}
		return false ;
	}

	//
	//def __tileSeriesFromTile250(tile250k):
	//mapsPerSeries = __mapsPerSeries(tile250k[1])
	//tileX = int(tile250k[0] / mapsPerSeries)
	//tileY = int(tile250k[1] / 4.0)
	//return tileX, tileY
	//

	private static int[] tileSeriesFromTile250(int[] tile250k) {
		int mapsPerSeries = mapsPerSeries(tile250k[1]) ;
		int tileX = tile250k[0] / mapsPerSeries ;
		int tileY = tile250k[1] / 4 ;
		return new int[] {tileX, tileY} ;
	}

	//def __tile250Y(lat):
	//return int(lat-40.0)
	//

	private static int tile250Y(double lat) {
		return (int)Math.round(Math.floor(lat-40.0)) ;
	}

	//def __tile250X(lon, lat):
	//width, offset = __widthAndOffset250(lat)
	//return int((lon + (144 - offset)) / width)
	//

	private static int tile250X(double lon, double lat) {
		double[] wo = widthAndOffset250(lat) ;
		return (int)Math.round(Math.floor((lon+(144-wo[1]))/wo[0])) ;
	}

	//def __tile250(lon, lat):
	//return __tile250X(lon, lat), __tile250Y(lat)  
	//

	private static int[] tile250(double lon, double lat) {
		return new int[] {tile250X(lon, lat), tile250Y(lat)} ;
	}

	//def __bounds250(tile):
	//minlat = tile[1] + 40.0
	//width, offset = __widthAndOffset250(minlat)
	//minlon = -144.0 + offset + (tile[0] * width)
	//maxlat = minlat + 1.0
	//maxlon = minlon + width
	//return Bounds(minlon, maxlon, minlat, maxlat)
	//

	private static Bounds bounds250(int[] tile) {
		double minlat = tile[1] + 40.0 ;
		double[] wo = widthAndOffset250(minlat) ;
		double minlon = -144 + wo[1] + (tile[0]*wo[0]) ;
		double maxlat = minlat + 1 ;
		double maxlon = minlon + wo[0] ;
		return new Bounds(minlon, maxlon, minlat, maxlat) ;
	}

	//def __ntsId250(tile250k):
	//seriesX, seriesY = __tileSeriesFromTile250(tile250k)
	//seriesNumber = __ntsIdSeries((seriesX, seriesY))
	//seriesMinYTile = int(seriesY * 4)
	//yTileInSeries = tile250k[1] - seriesMinYTile
	//
	//mapsPerSeries = __mapsPerSeries(tile250k[1])
	//seriesMinXTile = int(seriesX * mapsPerSeries)
	//xTileInSeries = tile250k[0] - seriesMinXTile
	//
	//if seriesY >= 10:
	//areaLetter = __MAP_250K_N_OF_68[yTileInSeries][xTileInSeries]
	//return seriesNumber, areaLetter
	//
	//if seriesY >= 7:
	//areaLetter = __MAP_250K_N_OF_68[yTileInSeries][xTileInSeries]
	//else:
	//areaLetter = __MAP_250K[yTileInSeries][xTileInSeries]
	//seriesRow = str(seriesY)
	//seriesX = 11 - seriesX
	//seriesColumn = str(seriesX)
	//if len(seriesColumn) == 1:
	//seriesColumn = "0" + seriesColumn
	//seriesNumber = seriesColumn + seriesRow
	//return seriesNumber, areaLetter
	//

	private static String[] ntsId250(int[] tile250k) {
		int[] tileS = tileSeriesFromTile250(tile250k) ;
		String seriesId = ntsIdSeries(tileS) ;
		int seriesMinYTile = tileS[1]*4 ;
		int yTileInSeries = tile250k[1]-seriesMinYTile ;

		int mapsPerSeries = mapsPerSeries(tile250k[1]) ;
		int seriesMinXTile = tileS[0] * mapsPerSeries ;
		int xTileInSeries = tile250k[0]-seriesMinXTile ;

		String areaLetter = null ;
		
		//TODO this is a diagnostic update so when crashes happen in the future, diagnostic info will be passed on with the crash report
		if(yTileInSeries < 0) {
			throw new ArrayIndexOutOfBoundsException("yTileInSeries < 0: seriesId" + seriesId + " tile250X:" + tile250k[0] + " tile250Y: " + tile250k[1]) ;
		} else if(xTileInSeries < 0) {
			throw new ArrayIndexOutOfBoundsException("xTileInSeries < 0: seriesId" + seriesId + " tile250X:" + tile250k[0] + " tile250Y: " + tile250k[1]) ;
		}
		
		if(tileS[1] >= 7) {
			areaLetter = MAP_250K_N_OF_68[yTileInSeries][xTileInSeries] ;	
		} else {
			areaLetter = MAP_250K[yTileInSeries][xTileInSeries] ;
		}

		return new String[] {seriesId, areaLetter} ;
	}

	//def __tile250ById(ntsId):
	//if len(ntsId) < 2:
	//raise ValueError, "Not enough arguments in ntsId to create 50k tile"
	//seriesTile = __tileSeriesById(ntsId[0])
	//area = ntsId[1].upper()
	//if seriesTile is not None:
	//seriesX, seriesY = seriesTile
	//if seriesY >= 7:
	//  valueMap = __MAP_250K_N_OF_68
	//else:
	//  valueMap = __MAP_250K
	//result = __indexXY(area, valueMap)
	//if result is not None:
	//  tileY = seriesY * 4 + result[1]
	//  mapsPerSeries = __mapsPerSeries(tileY)
	//  tileX = seriesX * mapsPerSeries + result[0]
	//  return tileX, tileY
	//else:
	//  raise ValueError, "Invalid area passed to __tile250ById()"
	//
	//else:
	//raise ValueError, "Invalid series passed to __tile250ById()"
	//

	private static int[] tile250ById(String[] ntsId) {
		if(ntsId.length < 2)
			throw new IllegalArgumentException("Not enough elements in ntsId to create tile!") ;
		int[] seriesT = tileSeriesById(ntsId[0]) ;
		String area = ntsId[1].toUpperCase() ;
		if(seriesT != null) {
			String[][] valueMap = MAP_250K ;
			if(seriesT[1] >= 7) {
				valueMap = MAP_250K_N_OF_68 ;
			}
			int[] result = indexXY(area, valueMap) ;
			if(result != null) {
				int tileY = seriesT[1] * 4 + result[1] ;
				int mapsPerSeries = mapsPerSeries(tileY) ;
				int tileX = seriesT[0] * mapsPerSeries + result[0] ;
				return new int[] {tileX, tileY} ;
			} else {
				throw new IllegalArgumentException("Invalid area") ;
			}
		} else {
			throw new IllegalArgumentException("Invalid series") ;
		}
	}

	//def __tiles250ByTileSeries(tileSeries):
	//minYTile = tileSeries[1] * 4
	//mapsPerSeries = __mapsPerSeries(minYTile)
	//minXTile = tileSeries[0] * mapsPerSeries
	//outList = []
	//for tileX in range(minXTile, minXTile+4):
	//for tileY in range(minYTile, minYTile+4):
	//  outList.append((tileX, tileY))
	//return outList

	private static List<int[]> tiles250ByTileSeries(int[] tileSeries) {
		int minYTile = tileSeries[1] * 4 ;
		int mapsPerSeries = mapsPerSeries(minYTile) ;
		int minXTile = tileSeries[0] * mapsPerSeries ;
		List<int[]> outlist = new ArrayList<int[]>() ;
		for(int tileX=minXTile; tileX<minXTile+4; tileX++) {
			for(int tileY=minYTile; tileY<minYTile+4; tileY++) {
				outlist.add(new int[]{tileX, tileY}) ;
			}
		}
		return outlist ;
	}

	//def __tile50ById(ntsId):
	//tile250k = __tile250ById(ntsId)
	//if len(ntsId) < 3:
	//raise ValueError, "Not enough arguments in ntsId to create 50k tile"
	//result = __indexXY(int(ntsId[2]), __MAP_50K)
	//if result is not None:
	//tileY = int(tile250k[1] * 4 + result[1])
	//tileX = int(tile250k[0] * 4 + result[0])
	//return tileX, tileY
	//else:
	//raise ValueError, "Invalid mapsheet passed to __tile250ById()"

	private static int[] tile50ById(String[] ntsId) {
		int[] tile250k = tile250ById(ntsId) ;
		if(ntsId.length < 3) {
			throw new IllegalArgumentException("Not enough arguments to create 50k tile") ;
		}

		int[] result = indexXY(String.valueOf(Integer.valueOf(ntsId[2])), MAP_50K) ;
		if(result != null) {
			int tileY = tile250k[1]*4+result[1] ;
			int tileX = tile250k[0]*4+result[0] ;
			return new int[]{tileX, tileY} ;
		} else {
			throw new IllegalArgumentException("Invalid mapsheet number") ;
		}
	}


	//def __tile250FromTile50(tile50k):
	//tileX = int(tile50k[0] / 4.0)
	//tileY = int(tile50k[1] / 4.0)
	//return tileX, tileY

	private static int[] tile250FromTile50(int[] tile50k) {
		int tileX = tile50k[0]/4 ;
		int tileY = tile50k[1]/4 ;
		return new int[] {tileX, tileY} ;
	}

	//def __tile50Y(lat):
	//tile250kY = __tile250Y(lat)
	//latDiff = lat - (tile250kY * 1.0 + 40.0)
	//plusTilesY = int(4.0 * latDiff) #height of area is always 1 degree
	//return 4 * tile250kY + plusTilesY

	private static int tile50Y(double lat) {
		int tile250kY = tile250Y(lat) ;
		double latDiff = lat - (tile250kY * 1.0 + 40) ;
		int plusTilesY = (int)Math.round(Math.floor(4.0*latDiff)) ;
		return 4*tile250kY + plusTilesY ;
	}

	//def __tile50X(lon, lat):
	//tile250kX = __tile250X(lon, lat)
	//width, offset = __widthAndOffset250(lat)
	//lonDiff = lon - (-144.0 + offset + (tile250kX * width))
	//plusTilesX = int(4.0 * lonDiff / width)
	//return 4 * tile250kX + plusTilesX

	private static int tile50X(double lon, double lat) {
		int tile250kX = tile250X(lon, lat) ;
		double[] wo = widthAndOffset250(lat) ;
		double lonDiff = lon - (-144.0+wo[1]+(tile250kX*wo[0])) ;
		int plusTilesX = (int)Math.round(Math.floor(4.0*lonDiff/wo[0])) ;
		return 4 * tile250kX + plusTilesX ;
	}

	//def __tile50(lon, lat):
	//return __tile50X(lon, lat), __tile50Y(lat)

	private static int[] tile50(double lon, double lat) {
		return new int[] {tile50X(lon, lat), tile50Y(lat)} ;
	}

	//def __ntsId50From250KTile(tile250k, plusTilesX, plusTilesY):
	//ntsId250k = __ntsId250(tile250k)
	//idInt = __MAP_50K[plusTilesY][plusTilesX]
	//if idInt >= 10:
	//return ntsId250k[0], ntsId250k[1], str(idInt)
	//else:
	//return ntsId250k[0], ntsId250k[1], "0" + str(idInt)

	private static String[] ntsId50From250KTile(int[] tile250k, int plusTilesX, int plusTilesY) {
		String[] ntsId250k = ntsId250(tile250k) ;
		String sheetId = MAP_50K[plusTilesY][plusTilesX] ;
		if(Integer.valueOf(sheetId) < 10) {
			sheetId = "0" + sheetId ;
		}
		return new String[] {ntsId250k[0], ntsId250k[1], sheetId} ;
	}

	//def __bounds50(tile50k, tile250k=None, returnId=False):
	//if tile250k is None:
	//tile250k = __tile250FromTile50(tile50k)
	//minXTile = tile250k[0] * 4
	//minYTile = tile250k[1] * 4
	//plusTilesX = tile50k[0] - minXTile
	//plusTilesY = tile50k[1] - minYTile
	//if returnId:
	//return __ntsId50From250KTile(tile250k, plusTilesX, plusTilesY)
	//else:
	//bounds250k = __bounds250(tile250k)
	//height = bounds250k.height() / 4.0
	//width = bounds250k.width() / 4.0
	//latDiff = plusTilesY * height
	//lonDiff = plusTilesX * width
	//minlat = bounds250k.miny() + latDiff
	//minlon = bounds250k.minx() + lonDiff
	//return Bounds(minlon, minlon+width, minlat, minlat+height)

	private static Bounds bounds50(int[] tile50k) {
		int[] tile250k = tile250FromTile50(tile50k) ;
		int minXTile = tile250k[0] * 4 ;
		int minYTile = tile250k[1] * 4 ;
		int plusTilesX = tile50k[0] - minXTile ;
		int plusTilesY = tile50k[1] - minYTile ;
		Bounds bounds250k = bounds250(tile250k) ;
		double height = bounds250k.height() / 4.0 ;
		double width = bounds250k.width() / 4.0 ;
		double latDiff = plusTilesY * height ;
		double lonDiff = plusTilesX * width ;
		double minlat = bounds250k.getMinY() + latDiff ;
		double minlon = bounds250k.getMinX() + lonDiff ;
		return new Bounds(minlon, minlon+width, minlat, minlat+height) ;
	}


	//def __ntsId50(tile50k, tile250k=None):
	//return __bounds50(tile50k, tile250k, True)    

	private static String[] ntsId50(int[] tile50k) {
		int[] tile250k = tile250FromTile50(tile50k) ;
		int minXTile = tile250k[0] * 4 ;
		int minYTile = tile250k[1] * 4 ;
		int plusTilesX = tile50k[0] - minXTile ;
		int plusTilesY = tile50k[1] - minYTile ;
		return ntsId50From250KTile(tile250k, plusTilesX, plusTilesY) ;
	}

	//def __tiles50ByTile250(tile250k):
	//minXTile = tile250k[0] * 4
	//minYTile = tile250k[1] * 4
	//outList = []
	//for tileX in range(minXTile, minXTile+4):
	//for tileY in range(minYTile, minYTile+4):
	//  outList.append((tileX, tileY))
	//return outList

	private static List<int[]> tiles50ByTile250(int[] tile250k) {
		int minYTile = tile250k[1] * 4 ;
		int minXTile = tile250k[0] * 4 ;
		List<int[]> outlist = new ArrayList<int[]>() ;
		for(int tileX=minXTile; tileX<minXTile+4; tileX++) {
			for(int tileY=minYTile; tileY<minYTile+4; tileY++) {
				outlist.add(new int[]{tileX, tileY}) ;
			}
		}
		return outlist ;
	}


	private static int tileBlockY(double lat) {
		int tile50kY = tile50Y(lat) ;
		double latDiff = lat - (tile50kY * 0.25 + 40) ;
		int plusTilesY = (int)Math.round(Math.floor(12.0*latDiff)) ;
		return 3 * tile50kY + plusTilesY ;
	}

	private static int tileBlockX(double lon, double lat) {
		int tile50kX = tile50X(lon, lat) ;
		double[] wo = widthAndOffset250(lat) ;
		double lonDiff = lon - (-144.0+wo[1]+(tile50kX*wo[0]/4.0)) ;
		int plusTilesX = (int)Math.round(Math.floor(16.0*lonDiff/wo[0])) ;
		return 4 * tile50kX + plusTilesX ;
	}

	private static int[] tileBlock(double lon, double lat) {
		return new int[] {tileBlockX(lon, lat), tileBlockY(lon)} ;
	}

	private static String[] ntsIdBlock(int[] tile) {
		int[] tile50k = tile50FromTileBlock(tile) ;
		int minXTile = tile50k[0] * 4 ;
		int minYTile = tile50k[1] * 3 ;
		int plusTilesX = tile[0] - minXTile ;
		int plusTilesY = tile[1] - minYTile ;
		String[] ntsId50 = ntsId50(tile50k) ;
		return new String[] {ntsId50[0], ntsId50[1], ntsId50[2], MAP_BLOCK[plusTilesY][plusTilesX]} ;
	}

	private static int[] tileBlockById(String[] ntsId) {
		int[] tile50k = tile50ById(ntsId) ;
		int[] xy = indexXY(ntsId[3], MAP_BLOCK) ;
		return new int[] {tile50k[0]*4+xy[0], tile50k[1]*3+xy[1]} ;
	}

	private static int[] tile50FromTileBlock(int[] tileBlock) {
		int tileX = tileBlock[0]/4 ;
		int tileY = tileBlock[1]/3 ;
		return new int[] {tileX, tileY} ;
	}

	private static Bounds boundsBlock(int[] tile) {
		int[] tile50k = tile50FromTileBlock(tile) ;
		Bounds bounds50 = bounds50(tile50k) ;
		double width = bounds50.width() / 4 ;
		double height = bounds50.height() / 3 ;

		int minXTile = tile50k[0] * 4 ;
		int minYTile = tile50k[1] * 3 ;
		int plusTilesX = tile[0] - minXTile ;
		int plusTilesY = tile[1] - minYTile ;

		double minX = bounds50.getMinX() + (plusTilesX*width) ;
		double minY = bounds50.getMinY() + (plusTilesY * height) ;

		return new Bounds(minX, minX + width, minY, minY+height) ;
	}

	private static List<int[]> tilesBlockByTile50(int[] tile50k) {
		int minYTile = tile50k[1] * 3 ;
		int minXTile = tile50k[0] * 4 ;
		List<int[]> outlist = new ArrayList<int[]>() ;
		for(int tileX=minXTile; tileX<minXTile+4; tileX++) {
			for(int tileY=minYTile; tileY<minYTile+3; tileY++) {
				outlist.add(new int[]{tileX, tileY}) ;
			}
		}

		return outlist ;
	}

	//if scale == SCALE_SERIES:
	//return __validTileSeries(tile)
	//elif scale == SCALE_250K:
	//return validTile(SCALE_SERIES, __tileSeriesFromTile250(tile))
	//elif scale == SCALE_50K:
	//return validTile(SCALE_250K, __tile250FromTile50(tile))

	private static boolean validTile(int scale, int[] tile) {
		if(tile == null)
			return false ;
		if(tile.length != 2)
			return false ;
		if(scale == SCALE_SERIES) {
			return validTileSeries(tile) ;
		} else if(scale == SCALE_250K) {
			return validTile(SCALE_SERIES, tileSeriesFromTile250(tile)) ;
		} else if(scale == SCALE_50K) {
			return validTile(SCALE_250K, tile250FromTile50(tile)) ;
		} else if(scale == SCALE_BLOCK) {
			return validTile(SCALE_50K, tile50FromTileBlock(tile)) ;
		}

		return false ;
	}

	//def tileByPoint(scale, point):
	//if BOUNDS.contains(point):
	//if scale == SCALE_SERIES:
	//  tile = __tileSeries(point[0], point[1])
	//elif scale == SCALE_250K:
	//  tile = __tile250(point[0], point[1])
	//elif scale == SCALE_50K:
	//  tile = __tile50(point[0], point[1])
	//else:
	//  raise ValueError, "No such scale constant"
	//if validTile(scale, tile):
	//  return tile
	//else:
	//  return None
	//else:
	//return None

	private static int[] tileByLatLon(int scale, LatLon l) {
		if(BOUNDS.contains(l)) {
			int tile[] = null ;
			switch(scale) {
			case SCALE_SERIES:
				tile = tileSeries(l.getLon(), l.getLat()) ;
				break ;
			case SCALE_250K:
				tile = tile250(l.getLon(), l.getLat()) ;
				break ;
			case SCALE_50K:
				tile = tile50(l.getLon(), l.getLat()) ;
				break ;
			case SCALE_BLOCK:
				tile = tileBlock(l.getLon(), l.getLat()) ;
				break ;
			}
			if(validTile(scale, tile)) {
				return tile ;
			} else {
				return null ;
			}
		} else {
			return null ;
		}
	}

	//def ntsId(scale, tile):
	//if scale == SCALE_SERIES:
	//return (__ntsIdSeries(tile),)
	//elif scale == SCALE_250K:
	//return __ntsId250(tile)
	//elif scale == SCALE_50K:
	//return __ntsId50(tile)
	//else:
	//raise ValueError, "No such scale constant"
	//

	private static String[] ntsId(int scale, int[] tile) {
		switch(scale) {
		case SCALE_SERIES:
			return new String[] {ntsIdSeries(tile)} ;
		case SCALE_250K:
			return ntsId250(tile) ;
		case SCALE_50K:
			return ntsId50(tile) ;
		case SCALE_BLOCK:
			return ntsIdBlock(tile) ;
		default:
			throw new IllegalArgumentException("Invalid scale: " + scale) ;
		}
	}

	//def ntsStringId(scale, tile):
	//if scale == SCALE_SERIES:
	//return __ntsIdSeries(tile)
	//elif scale == SCALE_250K:
	//ntsId = __ntsId250(tile)
	//elif scale == SCALE_50K:
	//ntsId = __ntsId50(tile)
	//else:
	//raise ValueError, "No such scale constant"
	//return "-".join(ntsId)

	private static String ntsStringId(int scale, int[] tile) {
		String[] ntsId = ntsId(scale, tile) ;
		String format = "" ;
		switch(scale) {
		case SCALE_SERIES:
			format = "%s" ;
			break ;
		case SCALE_250K:
			format = "%s-%s" ;
			break ;
		case SCALE_50K:
			format = "%s-%s-%s" ;
			break ;
		case SCALE_BLOCK:
			format = "%s-%s-%s-%s" ;
			break ;
		default:
			throw new IllegalArgumentException("Invalid scale: " + scale) ;
		}
		return String.format(format, (Object[])ntsId) ;
	}

	//def tileBounds(scale, tile):
	//if scale == SCALE_SERIES:
	//return __boundsSeries(tile)
	//elif scale == SCALE_250K:
	//return __bounds250(tile)
	//elif scale == SCALE_50K:
	//return __bounds50(tile)
	//else:
	//raise ValueError, "No such scale constant"

	//def tileById(ntsStringId):
	//ntsId = ntsStringId.split("-")
	//if len(ntsId) == 1:
	//scale = SCALE_SERIES
	//tile = __tileSeriesById(ntsId[0])
	//elif len(ntsId) == 2:
	//scale = SCALE_250K
	//tile = __tile250ById(ntsId)
	//elif len(ntsId) >= 3:
	//scale = SCALE_50K
	//tile = __tile50ById(ntsId)
	//if validTile(scale, tile):
	//return tile
	//else:
	//return None

	private static int scaleById(String ntsStringId) {
		String[] ntsId = ntsStringId.split("-") ;
		if(ntsId.length == 1) {
			return SCALE_SERIES ;
		} else if(ntsId.length == 2) {
			return SCALE_250K ;
		} else if(ntsId.length == 3) {
			return SCALE_50K ;
		} else if(ntsId.length == 4) {
			return SCALE_BLOCK ;
		} else {
			throw new IllegalArgumentException("Invalid NTS Id") ;
		}
	}

	private static int[] tileById(String ntsStringId) {
		String[] ntsId = ntsStringId.split("-") ;
		int scale = -1 ;
		int[] tile = null ;

		if(ntsId.length == 1) {
			scale = SCALE_SERIES ;
			tile = tileSeriesById(ntsId[0]) ;
		} else if(ntsId.length == 2) {
			scale = SCALE_250K ;
			tile = tile250ById(ntsId) ;
		} else if(ntsId.length == 3) {
			scale = SCALE_50K ;
			tile = tile50ById(ntsId) ;
		} else if(ntsId.length == 4) {
			scale = SCALE_BLOCK ;
			tile = tileBlockById(ntsId) ;
		} else {
			throw new IllegalArgumentException("Invalid NTS Id") ;
		}

		if(validTile(scale, tile)) {
			return tile ;
		} else {
			return null ;
		}
	}

	//def tileXAt(scale, point):
	//if scale == SCALE_SERIES:
	//return __tileSeriesX(point[0], point[1])
	//elif scale == SCALE_250K:
	//return __tile250X(point[0], point[1])
	//elif scale == SCALE_50K:
	//return __tile50X(point[0], point[1])
	//else:
	//raise ValueError, "No such scale constant"

	private static int tileXAt(int scale, double x, double y) {
		switch(scale) {
		case SCALE_SERIES:
			return tileSeriesX(x, y) ;
		case SCALE_250K:
			return tile250X(x, y) ;
		case SCALE_50K:
			return tile50X(x, y) ;
		case SCALE_BLOCK:
			return tileBlockX(x, y) ;
		default:
			throw new IllegalArgumentException("No such scale constant: " + scale) ;
		}
	}

	//def tileYAt(scale, lat):
	//if scale == SCALE_SERIES:
	//return __tileSeriesY(lat)
	//elif scale == SCALE_250K:
	//return __tile250Y(lat)
	//elif scale == SCALE_50K:
	//return __tile50Y(lat)
	//else:
	//raise ValueError, "No such scale constant"

	private static int tileYAt(int scale, double y) {
		switch(scale) {
		case SCALE_SERIES:
			return tileSeriesY(y) ;
		case SCALE_250K:
			return tile250Y(y) ;
		case SCALE_50K:
			return tile50Y(y) ;
		case SCALE_BLOCK:
			return tileBlockY(y) ;
		default:
			throw new IllegalArgumentException("No such scale constant: " + scale) ;
		}
	}

	//
	//def __loadTiles(scale, minx, miny, maxx, maxy, tilesList):
	//
	//tileMin = (tileXAt(scale, (minx, miny)), tileYAt(scale, miny))
	//tileMax = (tileXAt(scale, (maxx, maxy)), tileYAt(scale, maxy))
	//
	//for tileX in range(tileMin[0], tileMax[0]+1):
	//for tileY in range(tileMin[1], tileMax[1]+1):
	//  tile = (tileX, tileY)
	//  if validTile(scale, tile):
	//      tilesList.append(tile)

	private static void loadTiles(int scale, double minx, double miny, double maxx, double maxy, List<int[]> tileList) {
		int[] tileMin = new int[] {tileXAt(scale, minx, miny), tileYAt(scale, miny)} ;
		int[] tileMax = new int[] {tileXAt(scale, maxx, maxy), tileYAt(scale, maxy)} ;

		for(int tileX=tileMin[0]; tileX<=tileMax[0]; tileX++) {
			for(int tileY=tileMin[1]; tileY<=tileMax[1]; tileY++) {
				tileList.add(new int[] {tileX, tileY}) ;
			}
		}
	}

	//def tilesByBounds(scale, bounds):
	//minx = max(bounds.minx(), -144.0)
	//miny = max(bounds.miny(), 40.0)
	//maxx = min(bounds.maxx(), -48.0)
	//maxy = min(bounds.maxy(), 88.0)
	//
	//containsAbove80 = maxy > 80.0
	//containsAbove68 = containsAbove80 or maxy > 68.0 or miny > 68.0
	//containsBelow68 = miny < 68.0
	//containsBelow80 = miny < 80.0
	//
	//tiles = []
	//if containsAbove80:
	//if containsBelow80:
	//  __loadTiles(scale, minx, 80, maxx, maxy, tiles)
	//else:
	//  __loadTiles(scale, minx, miny, maxx, maxy, tiles)
	//  return tiles
	//if containsAbove68:
	//tempmaxy = maxy
	//tempminy = miny
	//if containsAbove80:
	//  tempmaxy = 79.99
	//if containsBelow68:
	//  tempminy = 68
	//__loadTiles(scale, minx, tempminy, maxx, tempmaxy, tiles)
	//if containsBelow68:
	//tempmaxy = maxy
	//if containsAbove68:
	//  tempmaxy = 67.99
	//__loadTiles(scale, minx, miny, maxx, tempmaxy, tiles)
	//return tiles

	private static final List<int[]> tilesByBounds(int scale, Bounds bounds) {
		double minx = Math.max(bounds.getMinX(), -144.0) ;
		double miny = Math.max(bounds.getMinY(), 40.0) ;
		double maxx = Math.min(bounds.getMaxX(), -48.0) ;
		double maxy = Math.min(bounds.getMaxY(), 88.0) ;

		boolean containsAbove80 = maxy > 80.0 ;
		boolean containsAbove68 = containsAbove80 || maxy > 68.0 || miny > 68;
		boolean containsBelow68 = miny < 68.0 ;
		boolean containsBelow80 = miny < 80.0 ;

		List<int[]> tiles = new ArrayList<int[]>() ;
		if(containsAbove80) {
			if(containsBelow80) {
				loadTiles(scale, minx, 80, maxx, maxy, tiles) ;
			} else {
				loadTiles(scale, minx, miny, maxx, maxy, tiles) ;
				return tiles ;
			}
		}

		double tempmaxy = maxy ;
		double tempminy = miny ;

		if(containsAbove68) {
			if(containsAbove80) {
				tempmaxy = 79.99 ;
			}
			if(containsBelow68) {
				tempminy = 68.0 ;
			}

			loadTiles(scale, minx, tempminy, maxx, tempmaxy, tiles) ;
		}

		if (containsBelow68) {
			tempmaxy = maxy ;
			if (containsAbove68)
				tempmaxy = 67.99 ;
			loadTiles(scale, minx, miny, maxx, tempmaxy, tiles) ;
		}

		return tiles ;

	}

	//def tileNames(*ntsIds):
	//tmpOutDir = tempfile.mkdtemp()
	//namesF = zipfile.ZipFile(os.path.dirname(__file__) + os.sep + "nts_names.zip")
	//extracted = []
	//names = []
	//for ntId in ntsIds:
	//try:
	//  fn = ntId[0]+".txt"
	//  if not fn in extracted:
	//      namesF.extract(fn, tmpOutDir)
	//      extracted.append(fn)
	//  fileh = open(tmpOutDir + os.sep + fn)
	//  targetId = "-".join(ntId).upper()
	//  for line in fileh:
	//      split = line.split("|")
	//      if split[0] == targetId:
	//          names.append(split[1].strip())
	//          break
	//  else:
	//      names.append(None)
	//  fileh.close()
	//except KeyError:
	//  names.append(None)
	//for fn in extracted:
	//os.remove(tmpOutDir + os.sep + fn)
	//os.rmdir(tmpOutDir)
	//namesF.close()
	//if len(names) == 1:
	//return names[0]
	//else:
	//return names
	//  
}
