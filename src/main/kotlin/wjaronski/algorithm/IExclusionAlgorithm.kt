package wjaronski.algorithm

interface IExclusionAlgorithm {
    val numberOfProcesses: Int

    fun requestCS()
    fun releaseCS()
}