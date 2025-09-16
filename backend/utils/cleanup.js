const MilkRecord = require("../models/MilkRecord")

/**
 * Delete milk records older than 6 months
 */
const cleanupOldRecords = async () => {
  try {
    const sixMonthsAgo = new Date()
    sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6)

    const result = await MilkRecord.deleteMany({
      createdAt: { $lt: sixMonthsAgo },
    })

    console.log(`🗑️ Deleted ${result.deletedCount} old milk records`)
    return result.deletedCount
  } catch (error) {
    console.error("Error during cleanup:", error)
    throw error
  }
}

/**
 * Get database statistics
 */
const getDatabaseStats = async () => {
  try {
    const totalRecords = await MilkRecord.countDocuments()
    const oldRecords = await MilkRecord.countDocuments({
      createdAt: { $lt: new Date(Date.now() - 6 * 30 * 24 * 60 * 60 * 1000) },
    })

    return {
      totalRecords,
      oldRecords,
      activeRecords: totalRecords - oldRecords,
    }
  } catch (error) {
    console.error("Error getting database stats:", error)
    throw error
  }
}

module.exports = {
  cleanupOldRecords,
  getDatabaseStats,
}
